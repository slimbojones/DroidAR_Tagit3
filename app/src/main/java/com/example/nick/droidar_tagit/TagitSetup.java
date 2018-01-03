package com.example.nick.droidar_tagit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import geo.GeoObj;
import gl.CustomGLSurfaceView;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.GLText;
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
import worldData.World;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TagitSetup extends Setup {

	private GLCamera camera;
	private World world;
	private Wrapper placeObjectWrapper;
	private static int RESULT_LOAD_IMAGE = 1;

	String textToDisplay = "enter text here";

	private GLText searchText;

	private GeoObj thisPosition;

	public ImageButton previewImage;

	public Obj placerContainer;

	private boolean placeMode = false;

	MeshComponent arrow;
	Bitmap myBitmap;



	public void updatePreviewImage(){


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

		GeoObj placerContainer = currentPosition;

		thisPosition = currentPosition.copy();

		//Obj placerContainer = new Obj();

		placerContainer.setComp(objectFactory.newArrow());
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);
		renderer.addRenderElement(world);

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
		eventManager.addOnLocationChangedAction(new ActionCalcRelativePos(
				world, camera));
	}

	@Override
	public void _d_addElementsToUpdateThread(SystemUpdater worldUpdater) {
		worldUpdater.addObjectToUpdateCycle(world);
	}

	@Override
	public void _e2_addElementsToGuiSetup(final GuiSetup guiSetup, Activity context) {

		//guiSetup.addButtonToTopView(new Command() {
//
		//	@Override
		//	public boolean execute() {
		//		final Obj placerContainer = new Obj();
		//		placerContainer.setComp(null);
		//		world.add(placerContainer);
		//		placeObjectWrapper.setTo(placerContainer);
		//		guiSetup.getTopView().getChildAt(0).setVisibility(View.GONE);
//
		//		return true;
		//	}
//
		//}, "Add Tag");

		guiSetup.addButtonToTopView(new Command() {

			@Override
			public boolean execute() {

				Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
				mediaChooser.setType("video/*, image/*");
				((Activity) myTargetActivity).startActivityForResult(mediaChooser,RESULT_LOAD_IMAGE );

				//IO.Settings s = new IO.Settings(getActivity(), "testSettings");
				//String stringKey = "skey";
				//String uriString = "";
				//uriString = s.loadString(stringKey);

				//Bitmap myBitmap = BitmapFactory.decodeFile(uriString);

				//previewImage.setImageBitmap(myBitmap);

				//Toast toast;
				//toast = Toast.makeText(getActivity().getBaseContext(), uriString, Toast.LENGTH_LONG);
				//toast.show();

				//previewImage.setImageResource(myBitmap);

				return true;
			}

		}, "");

		View pickImageButton = guiSetup.getTopView().getChildAt(0);
		if (pickImageButton instanceof Button) {

			((Button) pickImageButton).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_photo_black_24px, 0,0,0);

		}

		guiSetup.addButtonToTopView(new Command() {

			@Override
			public boolean execute() {

				View ibView = guiSetup.getBottomView().getChildAt(0);
				if (ibView instanceof EditText) {

					((EditText) ibView).setVisibility(View.VISIBLE);

					((EditText) ibView).requestFocusFromTouch();
					InputMethodManager lManager = (InputMethodManager)getActivity().getSystemService(getActivity().getBaseContext().INPUT_METHOD_SERVICE);
					lManager.showSoftInput(((EditText) ibView), 0);

				}

				return true;
			}

		}, "");

		View pickTextButton = guiSetup.getTopView().getChildAt(1);
		if (pickTextButton instanceof Button) {

			((Button) pickTextButton).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_comment_black_24px, 0,0,0);

		}

		guiSetup.addSearchbarToView(guiSetup.getBottomView(), new Command() {

			public boolean execute() {

				newTextObject();
				return false;
			}

			private Obj newTextObject() {
				final Obj placerContainer = new Obj();

				Vec textVec = new Vec(0,0,10);

				int textSize = 2;

				TextView v = new TextView(getActivity().getBaseContext());

				View ibView = guiSetup.getBottomView().getChildAt(0);
				if (ibView instanceof EditText) {

					if(((EditText) ibView).getText().toString().matches("")) {

						textToDisplay = "Tagit";
					}
					else {
						textToDisplay = ((EditText) ibView).getText().toString();
					}
				}

				//((EditText) ibView).setTextColor(Color.GREEN);

				v.setText(textToDisplay);
				v.setBackgroundColor(Color.DKGRAY);

				myBitmap = IO.loadBitmapFromView(v);

				previewImage.setImageBitmap(myBitmap);

				arrow = TagitFactory.getInstance().newTexturedSquare("textBitmap"
						+ textToDisplay, IO.loadBitmapFromView(v), textSize);
				arrow.setPosition(textVec);
				arrow.addAnimation(new AnimationFaceToCamera(camera));

				arrow.setOnClickCommand(new Command() {
					@Override
					public boolean execute() {
						placeObjectWrapper.setTo(placerContainer);
						return true;
					}
				});
				placerContainer.setComp(arrow);

				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);
				guiSetup.getBottomView().getChildAt(1).setVisibility(View.VISIBLE);
				guiSetup.getBottomView().getChildAt(2).setVisibility(View.VISIBLE);
				//guiSetup.getBottomView().getChildAt(0).setVisibility(View.GONE);

				return placerContainer;
			}
		}, textToDisplay);

		View myEditText = guiSetup.getBottomView().getChildAt(0);
		if (myEditText instanceof EditText) {


			((EditText) myEditText).setWidth (0);
			((EditText) myEditText).setHeight (0);
			((EditText) myEditText).getBackground().setAlpha(0);
			((EditText) myEditText).setClickable(false);

		}

		guiSetup.addImangeButtonToRightView(R.drawable.t_icon, new Command() {

			@Override
			public boolean execute() {
				//Toast toast;
				//toast = Toast.makeText(getActivity().getBaseContext(), "image button pressed", Toast.LENGTH_LONG);
				//toast.show();


				View ibView = guiSetup.getRightView().getChildAt(0);
				if (ibView instanceof ImageButton) {

					//Bitmap bitmap = ((BitmapDrawable)(((ImageButton) ibView)).getD
					Bitmap bitmap = ((BitmapDrawable)(((ImageButton) ibView)).getDrawable()).getBitmap();
					placeObjectFromSelector(bitmap);
					//placeMode = true;
					guiSetup.getBottomView().getChildAt(1).setVisibility(View.VISIBLE);
					guiSetup.getBottomView().getChildAt(2).setVisibility(View.VISIBLE);
				}

				return false;
				}

			}
		);

		guiSetup.addImangeButtonToBottomView(R.drawable.ic_check_circle_black_24px, new Command() {

			@Override
			public boolean execute() {

				final Obj placerContainer = new Obj();
				placerContainer.setComp(null);
				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);
				guiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
				guiSetup.getBottomView().getChildAt(2).setVisibility(View.GONE);

				return true;
			}

		});

		guiSetup.addImangeButtonToBottomView(R.drawable.ic_cancel_black_24px, new Command() {

			@Override
			public boolean execute() {

				//final Obj placerContainer = new Obj();
				//placerContainer.setComp(null);
				//world.add(placerContainer);
				//placeObjectWrapper.setTo(placerContainer);

				placerContainer.remove(arrow);
				//placeObjectWrapper.clear();

				guiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
				guiSetup.getBottomView().getChildAt(2).setVisibility(View.GONE);

				return true;
			}

		});

		guiSetup.setTopViewCentered();
		guiSetup.setBottomViewCentered();


		int buttHeight = 500;
		int buttWidth = 150;

		View ibView = guiSetup.getRightView().getChildAt(0);
		if (ibView instanceof ImageButton) {

			previewImage = ((ImageButton) ibView);
			//ImageButton imageB = (ImageButton) ibView;

			previewImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			// do what you want with imageView
		}

		guiSetup.getRightView().getLayoutParams().height = buttHeight;
		guiSetup.getRightView().getLayoutParams().width = buttWidth;
		guiSetup.getBottomView().getChildAt(1).setVisibility(View.GONE);
		guiSetup.getBottomView().getChildAt(2).setVisibility(View.GONE);


		final String[] itemname ={
				"Safari",
				"Camera",
				"Global",
				"Safari",
				"Camera",
				"Global",
				"Safari",
				"Camera",
				"Global",

		};

		Integer[] imgid={
				R.drawable.ic_cancel_black_24px,
				R.drawable.ic_font_download_white_24px,
				R.drawable.ic_insert_photo_black_24px,
				R.drawable.ic_cancel_black_24px,
				R.drawable.ic_font_download_white_24px,
				R.drawable.ic_insert_photo_black_24px,
				R.drawable.ic_cancel_black_24px,
				R.drawable.ic_font_download_white_24px,
				R.drawable.ic_insert_photo_black_24px,

		};


		ListView DynamicListView = new ListView(getActivity().getBaseContext());

		guiSetup.getLeftOuter().removeAllViews();

		DynamicListView.setDivider(null);
		DynamicListView.setDividerHeight(0);

		DynamicListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);

		//guiSetup.getLeftView().addView(DynamicListView);
		guiSetup.getLeftOuter().addView(DynamicListView);

		ViewGroup.LayoutParams params = guiSetup.getLeftOuter().getLayoutParams();
		//TODO change width to something dynamic and scalable
		params.width = 160;
		guiSetup.getLeftOuter().setLayoutParams(params);
		guiSetup.getLeftOuter().requestLayout();

		CustomListAdapter adapter=new CustomListAdapter(getActivity(), itemname, imgid);
		//list=(ListView)findViewById(R.id.list);
		DynamicListView.setAdapter(adapter);

		DynamicListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// TODO Auto-generated method stub
				String Selecteditem= itemname[+position];
				Toast.makeText(getActivity().getApplicationContext(), Selecteditem, Toast.LENGTH_SHORT).show();


			}
		});


	}

	public void placeObjectFromSelector(Bitmap selectedBitmap) {

		placerContainer = newPlacedObject(selectedBitmap);
		world.add(placerContainer);
		placeObjectWrapper.setTo(placerContainer);

	}

	public Obj newPlacedObject(Bitmap selectedBitmap) {

		placerContainer = new Obj();
		arrow = TagitFactory.getInstance()
					.newTexturedSquare(
							selectedBitmap.toString(),
							selectedBitmap,5);

		arrow.addAnimation(new AnimationFaceToCamera(camera));

		placerContainer.setComp(arrow);
		return placerContainer;

	}

}
