package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import actions.Action;
import actions.ActionBufferedCameraAR;
import actions.ActionCalcRelativePos;
import actions.ActionMoveCameraBuffered;
import actions.ActionPlaceObject;
import actions.ActionRotateCameraBuffered;
import commands.Command;
import commands.ui.CommandShowToast;
import geo.GeoObj;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.animations.AnimationFaceToCamera;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
import system.ErrorHandler;
import system.EventManager;
import system.Setup;
import util.IO;
import util.Vec;
import util.Wrapper;
import worldData.Obj;
import worldData.SystemUpdater;
import worldData.Updateable;
import worldData.World;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TagitSetup extends Setup implements View.OnLongClickListener, View.OnClickListener {

	private GLCamera camera;
	private World world;
	private Wrapper placeObjectWrapper;
	private static int RESULT_LOAD_IMAGE = 1;

	String textToDisplay = "enter text here";
	public Obj placerContainer;
	MeshComponent arrow;
	Bitmap myBitmap;
	private NameViewModel mModel;
	private PlacedTagModel ptModel;
	private GuiSetup thisGuiSetup;
	private RecyclerViewAdapter recyclerViewAdapter;

	Tagpost currentTagpost;

	Bitmap yourBitmap;

	private void toggleViews(){

		if(thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.VISIBLE){
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.VISIBLE);
		}
		else{
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.INVISIBLE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void _a_initFieldsIfNecessary() {

		// allow the user to send error reports to the developer:
		ErrorHandler.enableEmailReports("droidar.rwth@gmail.com",
				"Error in DroidAR App");
		placeObjectWrapper = new Wrapper();
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer renderer,
			GLFactory objectFactory, GeoObj currentPosition) {
		camera = new GLCamera(new Vec(0, 0, 10));
		world = new World(camera);
		renderer.addRenderElement(world);

		//loadFromPublicWorld();
	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager,
			CustomGLSurfaceView arView, SystemUpdater updater) {
		arView.addOnTouchMoveAction(new ActionBufferedCameraAR(camera));
		Action rot1 = new ActionRotateCameraBuffered(camera);
		//slimbo:  changed maxDistance from 50 to 20
		Action rot2 = new ActionPlaceObject(camera, placeObjectWrapper, 20);

		updater.addObjectToUpdateCycle(rot1);
		updater.addObjectToUpdateCycle(rot2);

		eventManager.addOnOrientationChangedAction(rot1);
		eventManager.addOnOrientationChangedAction(rot2);
		eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
				5, 25));
		//TODO find out if I need this or not
		//eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(
		//		world, camera));
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater worldUpdater) {
		worldUpdater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(final GuiSetup guiSetup, Activity context) {

		thisGuiSetup = guiSetup;

		//CREATE IMAGE PICKER BUTTON
		guiSetup.addImangeButtonToTopView(R.drawable.ic_insert_photo_black_24px, new Command() {

			@Override
			public boolean execute() {

				Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
				mediaChooser.setType("video/*, image/*");
				((Activity) myTargetActivity).startActivityForResult(mediaChooser,RESULT_LOAD_IMAGE );

				return true;
			}

		});

		//CREATE TEXT PICKER BUTTON
		guiSetup.addImangeButtonToTopView(R.drawable.ic_insert_comment_black_24px, new Command() {

			@Override
			public boolean execute() {

				View ibView = guiSetup.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {

					ibView.setVisibility(View.VISIBLE);

					ibView.requestFocusFromTouch();
					InputMethodManager lManager = (InputMethodManager)getActivity().getSystemService(getActivity().getBaseContext().INPUT_METHOD_SERVICE);
					lManager.showSoftInput((ibView), 0);
				}
				return true;
			}
		});

		//CREATE INVISIBLE EDITTEXT
		guiSetup.addSearchbarToView(guiSetup.getRightView(), new Command() {

			public boolean execute() {
				TextView v = new TextView(getActivity().getBaseContext());
				v.setBackgroundColor(Color.DKGRAY);
				View ibView = guiSetup.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {

					if(((EditText) ibView).getText().toString().matches("")) {
						textToDisplay = "Tagit";
					}
					else {
						textToDisplay = ((EditText) ibView).getText().toString();
					}
				}
				v.setText(textToDisplay);
				v.setShadowLayer(0.01f, -2, 2,  Color.BLACK);
				myBitmap = IO.loadBitmapFromView(v);

				ArActivity myActivity = (ArActivity) myTargetActivity;
				myActivity.addToUriPaths(textToDisplay, "type2");
				return false;
			}
		}, textToDisplay);

		View myEditText = guiSetup.getRightView().getChildAt(0);
		if (myEditText instanceof EditText) {

			((EditText) myEditText).setWidth (0);
			((EditText) myEditText).setHeight (0);
			((EditText) myEditText).getBackground().setAlpha(0);
			((EditText) myEditText).setClickable(false);
		}

		//CREATE CHECK BUTTON TO PLACE OBJECT
		guiSetup.addImangeButtonToBottomView(R.drawable.ic_check_circle_black_24px, new Command() {

			@Override
			public boolean execute() {

				final Obj placerContainer = new Obj();
				placerContainer.setComp(null);
				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);

				//TODO find a way to check what tagType it is
				String bitmapString = currentTagpost.getitemString();
				String typeString = currentTagpost.gettagType();

				Log.d ("PlacedTag", "on check position: " + camera.getPosition().toString() );
				Log.d ("PlacedTag", "on check rotation: " + camera.getRotation().toString() );
				Log.d ("PlacedTag", "on check GPSpostionvec: " + camera.getGPSPositionVec().toString() );
				Log.d ("PlacedTag", "on check new camera offset: " + camera.getNewCameraOffset().toString() );
				Log.d ("PlacedTag", "on check mynewposition: " + camera.getMyNewPosition().toString() );
				Log.d ("PlacedTag", "arrow getPosition: " + arrow.getPosition().toString() );

				String longString = Double.toString(arrow.getPosition().x);
				String latString = Double.toString(arrow.getPosition().y);
				String altString = Double.toString(arrow.getPosition().z);

				PlacedTag newPlacedTag = new PlacedTag(bitmapString, typeString, longString, latString, altString);
				ptModel.addPlacedTag(newPlacedTag);

				toggleViews();

				//loadFromPublicWorld();
				return true;
			}
		});

		//CREATE CANCEL BUTTON TO EXIT PLACEMENT
		guiSetup.addImangeButtonToBottomView(R.drawable.ic_cancel_black_24px, new Command() {

			@Override
			public boolean execute() {

				placerContainer.remove(arrow);
				toggleViews();
				return true;
			}
		});

		guiSetup.setTopViewCentered();
		guiSetup.setBottomViewCentered();

		guiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);
		guiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);

		android.view.ViewGroup.LayoutParams params2 = guiSetup.getBottomView().getChildAt(0).getLayoutParams();
		params2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		guiSetup.getTopView().getChildAt(0).setLayoutParams(params2);

		//TODO need a better way to set button size which is responsive to screen size
		guiSetup.getBottomView().getChildAt(0).setPadding(100,37,100,37);
		guiSetup.getBottomView().getChildAt(1).setPadding(100,37,100,37);
		guiSetup.getTopView().getChildAt(0).setPadding(100,37,100,37);
		guiSetup.getTopView().getChildAt(1).setPadding(100,37,100,37);

		guiSetup.getLeftOuter().removeAllViews();
		RecyclerView DynamicListView = new RecyclerView(getActivity().getBaseContext());
		guiSetup.getLeftOuter().addView(DynamicListView);

		ViewGroup.LayoutParams params = guiSetup.getLeftOuter().getLayoutParams();
		//TODO change width to something dynamic and scalable
		params.width = 160;
		guiSetup.getLeftOuter().setLayoutParams(params);
		guiSetup.getLeftOuter().requestLayout();

		View.OnLongClickListener myLongListener = this;
		View.OnClickListener myListener = this;

		mModel = ViewModelProviders.of((ArActivity) myTargetActivity).get(NameViewModel.class);

		recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<Tagpost>(), myLongListener, myListener, getActivity().getBaseContext() );
		DynamicListView.setLayoutManager(new LinearLayoutManager((ArActivity) myTargetActivity));
		DynamicListView.setAdapter(recyclerViewAdapter);

		mModel.getUriPathList().observe((ArActivity) myTargetActivity, new Observer<List<Tagpost>>() {
			@Override
			public void onChanged(@Nullable List<Tagpost> thisUriPathList) {
				recyclerViewAdapter.addItems(thisUriPathList);
			}
		});

		ptModel = ViewModelProviders.of((ArActivity) myTargetActivity).get(PlacedTagModel.class);
		ptModel.getPlacedTagList().observe((ArActivity) myTargetActivity, new Observer<List<PlacedTag>>() {
			@Override
			public void onChanged(@Nullable List<PlacedTag> getThosePlacedTags) {
				//TODO load only the new placedtags somehow?
				loadFromPublicWorld();
			}
		});

		//loadFromPublicWorld();

	}
	public void placeObjectFromSelector(Bitmap selectedBitmap) {

		placerContainer = new Obj();

		//Vec pos =camera.getGPSPositionVec();

		//final GeoObj placerContainer = new GeoObj(pos.y, pos.x, pos.z);

		//GeoObj placerContainer2 = new GeoObj(camera.getGPSLocation().getLongitude(), camera.getGPSLocation().getLatitude(), camera.getGPSLocation().getAltitude() );
		//placerContainer.setPosition(placerContainer2.getPosition());
		arrow = TagitFactory.getInstance().newTexturedSquare(selectedBitmap.toString(),selectedBitmap,5);

		arrow.setOnClickCommand(new CommandShowToast(myTargetActivity,
				"Item Found +1"));

		arrow.addAnimation(new AnimationFaceToCamera(camera));

		placerContainer.setComp(arrow);
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);
	}

	@Override
	public boolean onLongClick(View view) {
		final CharSequence[] items = {"Edit","Delete","Nevermind"};
		AlertDialog.Builder builder = new AlertDialog.Builder((ArActivity) myTargetActivity);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				switch(item) {
					case 0:
						ptModel.deleteAllPlacedTags();
						Toast.makeText(getActivity().getApplicationContext(), "Edits Coming Soon", Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Tagpost tagpost = (Tagpost) view.getTag();
						mModel.deleteTagpost(tagpost);
						break;
					default:
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

		return true;
	}

	@Override
	public void onClick(View view) {

		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

		currentTagpost = (Tagpost)view.getTag();
		yourBitmap = bitmapDrawable.getBitmap();
		//Toast.makeText(getActivity().getApplicationContext(), yourBitmap.toString(), Toast.LENGTH_SHORT).show();
		placeObjectFromSelector(yourBitmap);

		if(thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.GONE){
			toggleViews();
		}
	}

	public void loadFromPublicWorld(){

		world.getAllItems().clear();

		LiveData<List<PlacedTag>> list2;

		try {
			list2 = ptModel.getPlacedTagList();
			Log.d("PlacedTag", "first in list" + list2.toString());

			int listSize = list2.getValue().size();
			if(list2 !=null && listSize>0) {
				for (int j = 0; j < listSize; j++){

					PlacedTag thisPlacedTag = list2.getValue().get(j);
					spawnObj(thisPlacedTag);
				}
			}

			Toast.makeText(getActivity().getApplicationContext(), Integer.toString(listSize) + "items spawned", Toast.LENGTH_SHORT).show();
		}
		catch(Exception e){
			Log.d("PlacedTag", "tagitsetup 417" + e.toString());
		}


	}
	private void spawnObj(PlacedTag placedTag) {

		String bitmapString = placedTag.getbitmapString();
		String typeString = placedTag.gettagTypeString();
		String longString = placedTag.getlongString();
		String latString = placedTag.getlatString();
		String altString = placedTag.getaltString();

		GeoObj tempGeoObj = new GeoObj(Double.parseDouble(longString), Double.parseDouble(latString), Double.parseDouble(altString));
		Log.d("PlacedTag", "longString: " + longString);
		Log.d("PlacedTag", "latString: " + latString);
		Log.d("PlacedTag", "altString: " + altString);

		Bitmap tempBitmap;

		if(typeString.equals("type1")){

			tempBitmap = (IO.loadBitmapFromFile(bitmapString));
		}
		else{
			TextView v = new TextView(myTargetActivity);
			v.setTypeface(null, Typeface.BOLD);
			v.setShadowLayer(0.01f, -2, 2,  Color.BLACK);
			v.setText(bitmapString);
			tempBitmap = (IO.loadBitmapFromView(v));
		}

		MeshComponent mesh = TagitFactory.getInstance().newTexturedSquare(tempBitmap.toString(), tempBitmap, 5);


		placerContainer = new Obj();

		//camera;

		//mesh.setPosition(Vec.getNewRandomPosInXYPlane(
		//		camera.getPosition(), 12, 20));

		//mesh.setPosition(tempGeoObj.getPosition());
		mesh.setPosition(new Vec(Float.parseFloat(longString), Float.parseFloat(latString), Float.parseFloat(altString)));
		mesh.addChild(new AnimationFaceToCamera(camera, 0.5f));
		mesh.setOnClickCommand(new Command() {

			@Override
			public boolean execute() {

				world.remove(mesh);
				//Toast.makeText(getActivity().getApplicationContext(), "item removed", Toast.LENGTH_SHORT).show();

				return true;
			}

		});

		//mesh.addAnimation(new AnimationFaceToCamera(camera));
		placerContainer.setComp(mesh);
		//CommandShowToast.show(myTargetActivity, "Object spawned at "
		//		+ tempGeoObj.getMySurroundGroup().getPosition());
		world.add(placerContainer);
	}

}
