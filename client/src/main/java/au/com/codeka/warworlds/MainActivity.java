package au.com.codeka.warworlds;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;

import au.com.codeka.common.Log;
import au.com.codeka.warworlds.game.starfield.scene.StarfieldManager;
import au.com.codeka.warworlds.opengl.RenderSurfaceView;
import au.com.codeka.warworlds.ui.DrawerController;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The main activity of the whole app. All our fragments are children of this.
 */
// TODO: when there's no other activities, get rid of BaseActivity.
public class MainActivity extends BaseActivity {
  private static final Log log = new Log("MainActivity");

  private DrawerController drawerController;
  private StarfieldManager starfieldManager;
  private ImagePickerHelper imagePickerHelper;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Util.loadProperties();
    Util.setup(this);

    if (onBlueStacks()) {
      Toast.makeText(
          this,
          "Sorry, this platform is not supported. Please use a supported platform.",
          Toast.LENGTH_LONG).show();
      finish();
    }

    setContentView(R.layout.main_activity);
    setSupportActionBar(findViewById(R.id.toolbar));

    imagePickerHelper = new ImagePickerHelper(this);

    RenderSurfaceView renderSurfaceView = findViewById(R.id.render_surface);
    renderSurfaceView.create();
    starfieldManager = new StarfieldManager(renderSurfaceView);
    starfieldManager.create();
  }

  @Override
  protected void onStart() {
    super.onStart();

    NavController navController = Navigation.findNavController(this, R.id.main_content);

    final SharedPreferences prefs = Util.getSharedPreferences();
    if (!prefs.getBoolean("WarmWelcome", false)) {
      // if we've never done the warm-welcome, do it now
      log.info("Starting Warm Welcome");
      navController.navigate(R.id.warmWelcomeFragment);
    } else if (RealmContext.i.getCurrentRealm() == null) {
      log.info("No realm selected, switching to RealmSelectActivity");
      navController.navigate(R.id.realmSelectFragment);
    }

    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
    AppBarConfiguration appBarConfiguration =
        new AppBarConfiguration.Builder(R.id.starfieldFragment, R.id.solarSystemFragment)
            .setOpenableLayout(drawerLayout)
            .build();
    NavigationUI.setupWithNavController(
        findViewById(R.id.toolbar), navController, appBarConfiguration);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    drawerController = new DrawerController(
        navController, drawerLayout, findViewById(R.id.drawer_content));
    drawerController.start();

    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
      ActionBar actionBar = getSupportActionBar();
      if (actionBar == null) {
        return;
      }

      if (arguments != null && arguments.getBoolean("hideToolbar")) {
        actionBar.hide();
      } else {
        actionBar.show();
      }
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    drawerController.stop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    starfieldManager.destroy();
    starfieldManager = null;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (imagePickerHelper.onActivityResult(requestCode, resultCode, data)) {
      log.info("Image picker has returned a result.");
      // ImagePickerHelper should've called the correct callback...
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public ImagePickerHelper getImagePickerHelper() {
    return imagePickerHelper;
  }

  public StarfieldManager getStarfieldManager() {
    return starfieldManager;
  }

  public ActionBar requireSupportActionBar() {
    return checkNotNull(getSupportActionBar());
  }

  private boolean onBlueStacks() {
    File sharedFolder = new File(
        Environment.getExternalStorageDirectory(), "/windows/BstSharedFolder");
    return sharedFolder.exists();
  }
}