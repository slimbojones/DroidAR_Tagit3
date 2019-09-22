package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import actions.Action;
import actions.ActionCalcRelativePos;
import actions.ActionRotateCameraBuffered;
import commands.Command;
import commands.ui.CommandShowToast;
import components.ViewPosCalcerComp;
import geo.GeoObj;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.animations.AnimationFaceToCamera;
import gl.scenegraph.MeshComponent;
import gui.GuiSetup;
//import system.ErrorHandler;
import system.EventManager;
import system.Setup;
import util.Vec;
import util.Wrapper;
import v2.simpleUi.util.ErrorHandler;
import v2.simpleUi.util.IO;
import worldData.MoveComp;
import worldData.Obj;
import worldData.SystemUpdater;
import worldData.World;

public class TagitSetup extends Setup implements View.OnLongClickListener, View.OnClickListener {

	static final int REQUEST_IMAGE_CAPTURE = 3;
	private static int RESULT_LOAD_IMAGE = 1;
	private static int IMAGE_SEARCH_CODE = 5;
	public Obj placerContainer;
	String textToDisplay = "enter text here";
	MeshComponent arrow;
	Bitmap myBitmap;
	Tagpost currentTagpost;
	Bitmap yourBitmap;
	Map<Long, GeoObj> renderedArray = new HashMap<Long, GeoObj>();
	private GLCamera camera;
	private World world;
	private Wrapper placeObjectWrapper;
	private NameViewModel mModel;
	private PlacedTagModel ptModel;
	private GuiSetup thisGuiSetup;
	private RecyclerViewAdapter recyclerViewAdapter;
	private ViewPosCalcerComp viewPosCalcer;
	private MoveComp moveComp;
	private float objectDepth = 0.0f;

	private void toggleViews() {

		if (thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.VISIBLE) {
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(2).setVisibility(View.VISIBLE);
//TODO			thisGuiSetup.getTopView().getChildAt(4).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(3).setVisibility(View.GONE);
		} else {
			thisGuiSetup.getBottomView().getChildAt(0).setVisibility(View.VISIBLE);
			thisGuiSetup.getBottomView().getChildAt(1).setVisibility(View.VISIBLE);
			thisGuiSetup.getTopView().getChildAt(0).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(1).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(2).setVisibility(View.GONE);
//TODO			thisGuiSetup.getTopView().getChildAt(4).setVisibility(View.GONE);
			thisGuiSetup.getTopView().getChildAt(3).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void _a_initFieldsIfNecessary() {
		// allow the user to send error reports to the developer:
		ErrorHandler.enableEmailReports("droidar.rwth@gmail.com", "Error in DroidAR App");
		placeObjectWrapper = new Wrapper();
	}

	@Override
	public void _b_addWorldsToRenderer(GL1Renderer renderer, GLFactory objectFactory, GeoObj currentPosition) {
		camera = new GLCamera(new Vec(0, 0, 10));
		world = new World(camera);
		renderer.addRenderElement(world);
		placerContainer = new Obj();
	}

	@Override
	public void _c_addActionsToEvents(EventManager eventManager, CustomGLSurfaceView arView, SystemUpdater updater) {

		Action rot1 = new ActionRotateCameraBuffered(camera);

		viewPosCalcer = new ViewPosCalcerComp(camera, 150, 0.1f) {
			@Override
			public void onPositionUpdate(worldData.Updateable parent, Vec targetVec) {
				//Log.d("positionUpdate", "position: "+ viewPosCalcer.toString());
				if (parent instanceof Obj) {
					Obj obj = (Obj) parent;
					MoveComp m = obj.getComp(MoveComp.class);
					if (m != null) {

						float azimuthAngle;
						float ewValue;
						float nsValue;

						azimuthAngle = camera.getCameraAnglesInDegree()[0];

						ewValue = (float) Math.sin(azimuthAngle) * objectDepth;
						nsValue = (float) Math.cos(azimuthAngle) * objectDepth;

						targetVec = new Vec(targetVec.x + ewValue, targetVec.y + nsValue, targetVec.z);
						m.myTargetPos = targetVec;
						Log.d("m.myTargetPos", "m.myTargetPos: " + m.myTargetPos.toString());

						camera.showDebugInformation();
						Log.d("myCamera", "camera pos: " + camera.getPosition().toString());
						Log.d("myCamera", "camera rotMatrix: " + camera.getRotationMatrix().length);
						Log.d("myCamera", "camera getNewPosition: " + camera.getMyNewPosition().toString());
						Log.d("myCamera", "camera getCameraAnglesInDegree: " + camera.getCameraAnglesInDegree()[0]);
					}
				}
			}
		};
		moveComp = new MoveComp(4);

		updater.addObjectToUpdateCycle(rot1);

		eventManager.addOnOrientationChangedAction(rot1);

		//TODO find out if I need this or not
		eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(world, camera));

		//eventManager.addOnTrackballAction(new ActionMoveCameraBuffered(camera,
		//		5, 25));
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater worldUpdater) {
		worldUpdater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(final GuiSetup guiSetup, Activity context) {

		thisGuiSetup = guiSetup;

		//CREATE IMAGE PICKER BUTTON
		thisGuiSetup/*guiSetup*/.addImangeButtonToTopView(R.drawable.ic_insert_photo_black_24px, new Command() {

			@Override
			public boolean execute() {

				//TODO set to only images, no video

				Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
				mediaChooser.setType("video/*, image/*");
				((AppCompatActivity) getActivity()).startActivityForResult(mediaChooser, RESULT_LOAD_IMAGE);

				return true;
			}
		});

		//CREATE TEXT PICKER BUTTON
		thisGuiSetup/*guiSetup*/.addImangeButtonToTopView(R.drawable.ic_insert_comment_black_24px, new Command() {

			@Override
			public boolean execute() {

				View ibView = thisGuiSetup/*guiSetup*/.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {

					ibView.setVisibility(View.VISIBLE);

					ibView.requestFocusFromTouch();
					getActivity().getBaseContext();
					InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					lManager.showSoftInput((ibView), 0);
				}
				return true;
			}
		});

		//CREATE WEB PICKER BUTTON
		thisGuiSetup/*guiSetup*/.addImangeButtonToTopView(R.drawable.ic_public_black_24px, new Command() {

			@Override
			public boolean execute() {

				Intent i = new Intent(getActivity().getApplicationContext(), SearchActivity.class);

				//i.putExtra("result", imageResult);
				// TODO, wanted to test this

				getActivity().startActivityForResult(i, IMAGE_SEARCH_CODE);

				return true;
			}
		});
		/**
		 * TODO Removed for now
		 */
/*		guiSetup.addSeekbarToTopView(myTargetActivity.getResources().getDrawable(R.drawable.ic_location_on_black_24px), new Command() {

			@Override
			public boolean execute() {

				SeekBar mySeekbar = (SeekBar) guiSetup.getTopView().getChildAt(3);
				int distance = mySeekbar.getProgress();
				Toast.makeText(getActivity().getApplicationContext(), "distance: " + Integer.toString(distance), Toast.LENGTH_SHORT).show();

				if (placeObjectWrapper.getObject() instanceof Obj) {
					MoveComp mover = ((Obj) placeObjectWrapper.getObject())
							.getComp(MoveComp.class);
					if (mover != null) {
						float attenuation = .5f;

						objectDepth = (float) distance * attenuation;

						Log.d("objDepth", Float.toString(objectDepth));
						Toast.makeText(getActivity().getApplicationContext(), "mover.myTargetPos.y: " + Float.toString(mover.myTargetPos.y), Toast.LENGTH_SHORT).show();
					} else {
						Vec pos = ((Obj) placeObjectWrapper.getObject())
								.getPosition();
						if (pos != null) {

							pos.y = distance;
							Toast.makeText(getActivity().getApplicationContext(), "pos.y: " + Float.toString(pos.y), Toast.LENGTH_SHORT).show();
						} else {
							Log.e("objectDistance", "Cant move object, has no position!");
						}
					}
					return true;
				}
				return false;
			}
		});
*/
		//CREATE CAMERA BUTTON
		thisGuiSetup/*guiSetup*/.addImangeButtonToTopView(R.drawable.ic_photo_camera_black_24px, new Command() {

			@Override
			public boolean execute() {
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
					getActivity().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
				}

				return true;
			}
		});

		//CREATE INVISIBLE EDITTEXT
		thisGuiSetup/*guiSetup*/.addSearchbarToView(thisGuiSetup/*guiSetup*/.getRightView(), new Command() {

			public boolean execute() {
				TextView v = new TextView(getActivity().getBaseContext());
				v.setBackgroundColor(Color.DKGRAY);
				View ibView = thisGuiSetup/*guiSetup*/.getRightView().getChildAt(0);
				if (ibView instanceof EditText) {
					if (((EditText) ibView).getText().toString().matches("")) {
						textToDisplay = "Tagit";
					} else {
						textToDisplay = ((EditText) ibView).getText().toString();
					}
				}
				v.setText(textToDisplay);
				v.setShadowLayer(0.01f, -2, 2, Color.BLACK);
				myBitmap = IO.loadBitmapFromView(v);

				ArActivity myActivity = (ArActivity) getActivity();

				//TODO verify that link works and url is valid image file

				if (textToDisplay.startsWith("http://") || textToDisplay.startsWith("https://")) {
					myActivity.addToUriPaths(textToDisplay, "type3");
				} else {
					myActivity.addToUriPaths(textToDisplay, "type2");
				}

				return false;
			}
		}, textToDisplay);

		View myEditText = thisGuiSetup/*guiSetup*/.getRightView().getChildAt(0);
		if (myEditText instanceof EditText) {

			((EditText) myEditText).setWidth(0);
			((EditText) myEditText).setHeight(0);
			((EditText) myEditText).getBackground().setAlpha(0);
			((EditText) myEditText).setClickable(false);
		}

		//CREATE CHECK BUTTON TO PLACE OBJECT
		thisGuiSetup/*guiSetup*/.addImangeButtonToBottomView(R.drawable.ic_check_circle_black_24px, new Command() {

			@Override
			public boolean execute() {

				//final Obj placerContainer = new Obj();
				//placerContainer.setComp(null);
				//world.add(placerContainer);
				//placeObjectWrapper.setTo(placerContainer);
				placerContainer.remove(arrow);

//				MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.spray);
//				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				mPlayer.start();

				//TODO find a way to check what tagType it is
				String bitmapString = currentTagpost.getitemString();
				String typeString = currentTagpost.gettagType();

				Log.d("PlacedTag", "on check position: " + camera.getPosition().toString());
				Log.d("PlacedTag", "on check rotation: " + camera.getRotation().toString());
				Log.d("PlacedTag", "on check GPSpostionvec: " + camera.getGPSPositionVec().toString());
				Log.d("PlacedTag", "on check new camera offset: " + camera.getNewCameraOffset().toString());
				Log.d("PlacedTag", "on check mynewposition: " + camera.getMyNewPosition().toString());
				Log.d("PlacedTag", "arrow getPosition: " + arrow.getPosition().toString());

				GeoObj newGeo = new GeoObj();
				newGeo.setVirtualPosition(arrow.getPosition());
				newGeo.setComp(arrow);
				newGeo.refreshVirtualPosition();

				double longString = newGeo.getLongitude();
				double latString = newGeo.getLatitude();
				double altString = arrow.getPosition().z;

				PlacedTag newPlacedTag = new PlacedTag(bitmapString, typeString, longString, latString, altString);
				ptModel.addPlacedTag(newPlacedTag);

				world.remove(newGeo);

				toggleViews();

				return true;
			}
		});

		//CREATE CANCEL BUTTON TO EXIT PLACEMENT
		thisGuiSetup/*guiSetup*/.addImangeButtonToBottomView(R.drawable.ic_cancel_black_24px, new Command() {

			@Override
			public boolean execute() {
				placerContainer.remove(arrow);
				toggleViews();
				return true;
			}
		});

		thisGuiSetup/*guiSetup*/.setTopViewCentered();
		thisGuiSetup/*guiSetup*/.setBottomViewCentered();

		thisGuiSetup/*guiSetup*/.getBottomView().getChildAt(0).setVisibility(View.GONE);
		thisGuiSetup/*guiSetup*/.getBottomView().getChildAt(1).setVisibility(View.GONE);

		//android.view.ViewGroup.LayoutParams params2 = guiSetup.getBottomView().getChildAt(0).getLayoutParams();
		//params2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		//guiSetup.getTopView().getChildAt(0).setLayoutParams(params2);

		//TODO need a better way to set button size which is responsive to screen size
		int buttonPadding = 0;

		thisGuiSetup/*guiSetup*/.getBottomView().getChildAt(0).setPadding(100, buttonPadding, 100, buttonPadding);
		thisGuiSetup/*guiSetup*/.getBottomView().getChildAt(1).setPadding(100, buttonPadding, 100, buttonPadding);
		thisGuiSetup/*guiSetup*/.getTopView().getChildAt(0).setPadding(100, buttonPadding, 100, buttonPadding);
		thisGuiSetup/*guiSetup*/.getTopView().getChildAt(1).setPadding(100, buttonPadding, 100, buttonPadding);
		thisGuiSetup/*guiSetup*/.getTopView().getChildAt(2).setPadding(100, buttonPadding, 100, buttonPadding);
//TODO		thisGuiSetup/*guiSetup*/.getTopView().getChildAt(4).setPadding(100, buttonPadding, 100, buttonPadding);

		thisGuiSetup/*guiSetup*/.getLeftOuter().removeAllViews();
		RecyclerView DynamicListView = new RecyclerView(getActivity().getBaseContext());
		thisGuiSetup/*guiSetup*/.getLeftOuter().addView(DynamicListView);

		ViewGroup.LayoutParams params = thisGuiSetup/*guiSetup*/.getLeftOuter().getLayoutParams();
		//TODO change width to something dynamic and scalable
		params.width = 160;
		thisGuiSetup/*guiSetup*/.getLeftOuter().setLayoutParams(params);
		thisGuiSetup/*guiSetup*/.getLeftOuter().requestLayout();

		View.OnLongClickListener myLongListener = this;
		View.OnClickListener myListener = this;

		mModel = ViewModelProviders.of((ArActivity) getActivity()).get(NameViewModel.class);

		recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<Tagpost>(), myLongListener, myListener, getActivity().getBaseContext());
		DynamicListView.setLayoutManager(new LinearLayoutManager((ArActivity) getActivity()));
		DynamicListView.setAdapter(recyclerViewAdapter);

		mModel.getUriPathList().observe((ArActivity) getActivity(), new Observer<List<Tagpost>>() {
			@Override
			public void onChanged(@Nullable List<Tagpost> thisUriPathList) {
				recyclerViewAdapter.addItems(thisUriPathList);
			}
		});

		ptModel = ViewModelProviders.of((ArActivity) getActivity()).get(PlacedTagModel.class);
		ptModel.getPlacedTagList().observe((ArActivity) getActivity(), new Observer<List<PlacedTag>>() {
			@Override
			public void onChanged(@Nullable List<PlacedTag> getThosePlacedTags) {
				//TODO load only the new placedtags somehow?
				loadFromPublicWorld();
			}
		});
	}

	//Clicking item on toolbelt
	@Override
	public void onClick(View view) {

		objectDepth = 0.0f;

//		MediaPlayer mPlayer = MediaPlayer.create(myTargetActivity, R.raw.shake);
//		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//		mPlayer.start();

		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

		currentTagpost = (Tagpost) view.getTag();
		yourBitmap = bitmapDrawable.getBitmap();

		//Toast.makeText(getActivity().getApplicationContext(), yourBitmap.toString(), Toast.LENGTH_SHORT).show();
		placeObjectFromSelector(yourBitmap);

		if (thisGuiSetup.getBottomView().getChildAt(0).getVisibility() == View.GONE) {
			toggleViews();
		}
	}

	public void placeObjectFromSelector(Bitmap selectedBitmap) {
		arrow = TagitFactory.getInstance().newTexturedSquare(selectedBitmap.toString(), selectedBitmap, 5);
		arrow.setOnClickCommand(new CommandShowToast(getActivity(),"Item Found +1"));
		arrow.addAnimation(new AnimationFaceToCamera(camera));

		placerContainer.setComp(viewPosCalcer);
		placerContainer.setComp(moveComp);

		placerContainer.setComp(arrow);
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);
	}

	@Override
	public boolean onLongClick(View view) {
		final CharSequence[] items = {"Delete ALL From Toolbelt", "Delete From Toolbelt", "Nevermind"};
		AlertDialog.Builder builder = new AlertDialog.Builder((ArActivity) getActivity());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				switch (item) {
					case 0:
						mModel.deleteAllTagposts();
						Toast.makeText(getActivity().getApplicationContext(), "Toolbelt Cleared", Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Tagpost tagpost = (Tagpost) view.getTag();
						mModel.deleteTagpost(tagpost);
						Toast.makeText(getActivity().getApplicationContext(), "Removed from Tooolbelt", Toast.LENGTH_SHORT).show();
						break;
					default:
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

		return true;
	}

	public void loadFromPublicWorld() {

		//world.getAllItems().clear();

		LiveData<List<PlacedTag>> list2;
		int placedCount = 0;
		Set<Long> worldTagIds = new HashSet<>();

		try {
			list2 = ptModel.getPlacedTagList();
			Log.d("PlacedTag", "first in list" + list2.toString());
			Log.d("loadPW", "renderedArraybefore: " + renderedArray.toString());

			int listSize = list2.getValue().size();
			if (list2 != null && listSize > 0) {
				for (int j = 0; j < listSize; j++) {

					PlacedTag thisPlacedTag = list2.getValue().get(j);
					int tagId = thisPlacedTag.getid();
					//worldTagIds is a list of everything that SHOULD be rendered
					worldTagIds.add((long) tagId);

					//Log.d("loadPW", "placedGeoObj: " + placedGeoObj.toString());
					if (renderedArray.containsKey((long) tagId)) {
						Log.d("loadPW", "geoObj already exists: " + tagId);
						//already rendered, skip and try the next one
					} else {
						spawnObj(thisPlacedTag);
						placedCount++;
					}
				}
			}

			Map<Long, GeoObj> renderedArrayCopy = new HashMap<>();
			renderedArrayCopy = new HashMap<Long, GeoObj>(renderedArray);
			//the following is a list of everything that is currently rendered that needs to be removed
			renderedArrayCopy.keySet().removeAll(worldTagIds);
			Log.d("loadPW", "worldTagids: " + worldTagIds.toString());
			Log.d("loadPW", "renderedArrayCopy: " + renderedArrayCopy.toString());

			for (Map.Entry<Long, GeoObj> entry : renderedArrayCopy.entrySet()) {
				Long key = entry.getKey();
				GeoObj value = entry.getValue();
				Log.d("loadPW", "geoobj from renderedArray: " + renderedArray.get(key).toString());
				renderedArray.remove(key);
				world.remove(value);
				Log.d("loadPW", "removed item: " + key);
			}
			Log.d("loadPW", "world efficientList: " + world.getAllItems().toString());
			Log.d("loadPW", "renderedArrayafter: " + renderedArray.toString());
			Toast.makeText(getActivity().getApplicationContext(), placedCount + "item(s) spawned", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.d("PlacedTag", "tagitsetup 417" + e.toString());
		}
	}

	private void spawnObj(PlacedTag placedTag) {

		String bitmapString = placedTag.getbitmapString();
		String typeString = placedTag.gettagTypeString();
		double longString = placedTag.getlongString();
		double latString = placedTag.getlatString();
		double altString = placedTag.getaltString();
		long ptId = (long) placedTag.getid();

		final Bitmap[] tempBitmap = new Bitmap[1];

		if (typeString.equals("type1")) {
			tempBitmap[0] = (IO.loadBitmapFromFile(bitmapString/*, 2*/));
		} else if (typeString.equals("type2")) {
			TextView v = new TextView(getActivity());
			v.setBackgroundColor(Color.DKGRAY);
			v.setTypeface(null, Typeface.BOLD);
			v.setShadowLayer(0.01f, -2, 2, Color.BLACK);
			v.setText(bitmapString);
			tempBitmap[0] = (IO.loadBitmapFromView(v));
		} else {
			Uri myURI = Uri.parse(bitmapString);
			//Picasso.with(context).load(myURI).into(holder.imageView);
			Picasso.get()
				   .load(myURI)
				   .into(new Target() {
					   @Override
					   public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
						   tempBitmap[0] = bitmap;
					   }

					   @Override
					   public void onPrepareLoad(Drawable placeHolderDrawable) {}

					   @Override
					   public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
				   });
			//tempBitmap = IO.loadBitmapFromURL(tagString);
			//util.Log.d("imageURL", "RVA tagstring: " + tagString);
		}

		MeshComponent mesh = TagitFactory.getInstance().newTexturedSquare(tempBitmap[0].toString(), tempBitmap[0], 5);
		//tempBitmap[0].recycle();
		GeoObj newGeo2 = new GeoObj(latString, longString, altString, mesh, true);

		renderedArray.put(ptId, newGeo2);
		Log.d("loadPW", "spawnObj mesh: " + mesh.toString());
		Log.d("loadPW", "added to renderedArray2: " + ptId);
		Log.d("loadPW", "spawnObj renderedArray: " + renderedArray.toString());

		//mesh.setPlacedTagId(placedTag.getid());

		mesh.addChild(new AnimationFaceToCamera(camera, 0.5f));
		mesh.setOnClickCommand(new Command() {

			@Override
			public boolean execute() {
				//ptModel.deletePlacedTag(placedTag);
				ArActivity myActivity = (ArActivity) getActivity();
				myActivity.showAlertDialog(placedTag.getid());
				return true;
			}
		});
		world.add(newGeo2);
	}
}
