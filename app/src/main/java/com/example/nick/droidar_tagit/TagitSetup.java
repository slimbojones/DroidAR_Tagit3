package com.example.nick.droidar_tagit;

import android.app.Activity;
import android.content.Intent;
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

public class TagitSetup extends Setup {

	private GLCamera camera;
	private World world;
	private Wrapper placeObjectWrapper;
	private static int RESULT_LOAD_IMAGE = 1;

	String textToDisplay = "default";

	private GLText searchText;

	private GeoObj thisPosition;

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

		Toast toast;
		toast = Toast.makeText(getActivity().getBaseContext(), String.valueOf(currentPosition.getLatitude()), Toast.LENGTH_LONG);
		toast.show();
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
	public void _e2_addElementsToGuiSetup(GuiSetup guiSetup, Activity context) {

		guiSetup.addButtonToTopView(new Command() {
			@Override
			public boolean execute() {
				final Obj placerContainer = newObject();
				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);
				return true;
			}

			private Obj newObject() {
				final Obj placerContainer = new Obj();

				String uriString;
				uriString = null;

				MeshComponent arrow;

				IO.Settings s = new IO.Settings(getActivity(), "testSettings");
				String stringKey = "skey";
				uriString = s.loadString(stringKey);

				if( uriString == null) {

					arrow = TagitFactory.getInstance()
							.newTexturedSquare(
									"iconId",
									IO.loadBitmapFromId(myTargetActivity,
											R.drawable.t_icon));

				}
				else{

					arrow = TagitFactory.getInstance()
							.newTexturedSquare(
									"customId",
									IO.loadBitmapFromFile(uriString));

					arrow.addAnimation(new AnimationFaceToCamera(camera));

				}

				arrow.setOnClickCommand(new Command() {
					@Override
					public boolean execute() {
						placeObjectWrapper.setTo(placerContainer);
						return true;
					}
				});
				placerContainer.setComp(arrow);
				return placerContainer;
			}
		}, "Add Tag");

		guiSetup.addButtonToTopView(new Command() {

			@Override
			public boolean execute() {

				Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
				mediaChooser.setType("video/*, image/*");
				((Activity) myTargetActivity).startActivityForResult(mediaChooser,RESULT_LOAD_IMAGE );

				return true;
			}

		}, "Pick Tag");

		guiSetup.addButtonToTopView(new Command() {

			public boolean execute() {
				final Obj placerContainer = newTextObject();
				world.add(placerContainer);
				placeObjectWrapper.setTo(placerContainer);
				return true;
			}

			private Obj newTextObject() {
				final Obj placerContainer = new Obj();

				Vec textVec = new Vec(0,0,20);
				MeshComponent textComp;
				int textSize = 2;

				TextView v = new TextView(getActivity().getBaseContext());
				v.setText(textToDisplay);

				textComp = TagitFactory.getInstance().newTexturedSquare("textBitmap"
						+ textToDisplay, IO.loadBitmapFromView(v), textSize);
				textComp.setPosition(textVec);
				textComp.addAnimation(new AnimationFaceToCamera(camera));

				textComp.setOnClickCommand(new Command() {
					@Override
					public boolean execute() {
						placeObjectWrapper.setTo(placerContainer);
						return true;
					}
				});
				placerContainer.setComp(textComp);
				return placerContainer;
			}

		}, "Pick Text");

		guiSetup.addSearchbarToView(guiSetup.getBottomView(), new Command() {

			@Override
			public boolean execute() {
				return false;
			}

			@Override
			public boolean execute(Object transfairObject) {
				if (transfairObject instanceof String) {
					textToDisplay = (String) transfairObject;
					//if (searchText != null)
					//	searchText.changeTextTo(textToDisplay);

				}
				return true;
			}
		}, textToDisplay);

		guiSetup.setTopViewCentered();
	}

}
