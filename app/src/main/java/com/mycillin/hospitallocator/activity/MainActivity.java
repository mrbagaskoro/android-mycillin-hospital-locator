package com.mycillin.hospitallocator.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mycillin.hospitallocator.R;
import com.mycillin.hospitallocator.adapter.DialogImagePickerAdapter;
import com.mycillin.hospitallocator.rest.MyCillinAPI;
import com.mycillin.hospitallocator.rest.MyCillinRestClient;
import com.mycillin.hospitallocator.rest.addFacilityInfo.ModelResultAddFacilityInfo;
import com.mycillin.hospitallocator.util.ProgressBarHandler;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainActivity_rl_mainContainer)
    RelativeLayout mainContainer;
    @BindView(R.id.mainActivity_rl_formContainer)
    RelativeLayout formContainer;

    @BindView(R.id.mainActivity_tv_address)
    TextView addressInfoTxt;
    @BindView(R.id.mainActivity_tv_coordinates)
    TextView coordinatesInfoTxt;
    @BindView(R.id.mainActivity_bt_nextBtn)
    Button nextBtn;

    @BindView(R.id.mainActivity_et_facilityName)
    EditText facilityNameEdtxt;
    @BindView(R.id.mainActivity_tv_facilityAddress)
    TextView facilityAddressTxt;
    @BindView(R.id.mainActivity_et_facilityPhone)
    EditText facilityPhoneEdtxt;
    @BindView(R.id.mainActivity_tv_facilityLatitude)
    TextView facilityLatitudeTxt;
    @BindView(R.id.mainActivity_tv_facilityLongitude)
    TextView facilityLongitudeTxt;
    @BindView(R.id.mainActivity_iv_facilityPic)
    ImageView facilityPicImg;
    @BindView(R.id.mainActivity_iv_photoIcon)
    ImageView addFacilityPicImg;
    @BindView(R.id.mainActivity_tv_imageWarning)
    TextView imageWarningTxt;
    @BindView(R.id.mainActivity_ib_clearPicture)
    ImageButton clearFacilityPicImgButton;
    @BindView(R.id.mainActivity_bt_saveBtn)
    Button saveBtn;

    private boolean doubleBackToExitPressedOnce = false;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private Bitmap bitmapResult = null;

    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int PERMISSIONS_REQUEST_CODE = 9001;
    public static final int REQUEST_CODE_GALLERY = 1002;
    public static final int REQUEST_CODE_CAMERA = 1003;

    private ProgressBarHandler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressBarHandler = new ProgressBarHandler(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSIONS_REQUEST_CODE);

                            return;
                        }
                        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


                        if(mLocation != null) {
                            currentLatitude = mLocation.getLatitude();
                            currentLongitude = mLocation.getLongitude();

                            addressInfoTxt.setText(getAdressFromCoordinates(currentLatitude, currentLongitude));
                            coordinatesInfoTxt.setText("( " + currentLatitude + ", " + currentLongitude + " )");
                        }

                        startLocationUpdates();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();

        addFacilityPicImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog();
            }
        });

        clearFacilityPicImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapResult = null;
                imageWarningTxt.setVisibility(View.INVISIBLE);
                addFacilityPicImg.setVisibility(View.VISIBLE);
                facilityPicImg.setImageResource(android.R.color.transparent);
                facilityPicImg.setVisibility(View.GONE);
                clearFacilityPicImgButton.setVisibility(View.INVISIBLE);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainContainer.setVisibility(View.GONE);
                formContainer.setVisibility(View.VISIBLE);

                facilityAddressTxt.setText(getAdressFromCoordinates(currentLatitude, currentLongitude));
                facilityLatitudeTxt.setText(currentLatitude + "");
                facilityLongitudeTxt.setText(currentLongitude + "");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;

                if(facilityNameEdtxt.getText().toString().trim().equals("")) {
                    facilityNameEdtxt.setError("Facility Name is Required !");
                    isValid = false;
                }
                if(facilityPhoneEdtxt.getText().toString().trim().equals("")) {
                    facilityPhoneEdtxt.setError("Facility Phone Number is Required !");
                    isValid = false;
                }
                if(bitmapResult == null) {
                    imageWarningTxt.setVisibility(View.VISIBLE);
                    isValid = false;
                }

                if(isValid) {
                    imageWarningTxt.setVisibility(View.INVISIBLE);

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Add Medical Facility")
                            .setMessage("Are You Sure ?")
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addFacilityInfo(facilityNameEdtxt.getText().toString(), facilityAddressTxt.getText().toString(),
                                            facilityPhoneEdtxt.getText().toString(), facilityLatitudeTxt.getText().toString(),
                                            facilityLongitudeTxt.getText().toString(), bitmapResult);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPlayServices()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.mainActivity_infoTitle)
                    .setMessage(R.string.mainActivity_googlePlayServiceWarningMessage)
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton(getString(R.string.mainActivity_OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, new com.google.android.gms.location.LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if(location != null) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();

                                addressInfoTxt.setText(getAdressFromCoordinates(currentLatitude, currentLongitude));
                                coordinatesInfoTxt.setText("( " + currentLatitude + ", " + currentLongitude + " )");
                            }
                        }
                    });
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_CODE_GALLERY) {
                try {
                    Uri selectedImage = data.getData();
                    bitmapResult = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == REQUEST_CODE_CAMERA) {
                bitmapResult = (Bitmap) data.getExtras().get("data");
            }

            addFacilityPicImg.setVisibility(View.GONE);
            facilityPicImg.setVisibility(View.VISIBLE);
            facilityPicImg.setImageBitmap(bitmapResult);
            imageWarningTxt.setVisibility(View.INVISIBLE);
            clearFacilityPicImgButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        if(mainContainer.getVisibility() == View.VISIBLE) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press Back again to leave", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
        else {
            backToMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_REQUEST_CODE) {
            if(grantResults.length < 0) {
                finish();
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                finish();

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if(location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            addressInfoTxt.setText(getAdressFromCoordinates(currentLatitude, currentLongitude));
                            coordinatesInfoTxt.setText("( " + currentLatitude + ", " + currentLongitude + " )");
                        }
                    }
                });


    }

    private String getAdressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String res = "";

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses.size() > 0) {
                res = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    private void showImagePickerDialog() {
        DialogImagePickerAdapter dialogImagePickerAdapter = new DialogImagePickerAdapter(MainActivity.this);
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new GridHolder(2))
                .setHeader(R.layout.dialog_image_picker_header)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .setAdapter(dialogImagePickerAdapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        if(position == 0) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , REQUEST_CODE_GALLERY);
                            dialog.dismiss();
                        }
                        else if(position == 1) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, REQUEST_CODE_CAMERA);
                            dialog.dismiss();
                        }
                    }
                })
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        dialog.show();
    }

    private void addFacilityInfo(String facilityName, String address, String phoneNo, String latitude, String longitude, final Bitmap bitmap) {
        try {
            progressBarHandler.show();

            MyCillinAPI myCillinAPI = MyCillinRestClient.getMyCillinRestInterface();

            String fileName = "img_" + facilityName;

            File file = new File(getApplicationContext().getFilesDir(), fileName + ".jpg");

            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            RequestBody facilityNameBody = RequestBody.create(MediaType.parse("text/plain"), facilityName);
            RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
            RequestBody phoneNoBody = RequestBody.create(MediaType.parse("text/plain"), phoneNo);
            RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), latitude);
            RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), longitude);

            RequestBody facilityImgFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part facilityImg = MultipartBody.Part.createFormData("facility_pict", file.getName(), facilityImgFile);

            myCillinAPI.addFacilityInformation(facilityNameBody, addressBody, phoneNoBody, latitudeBody, longitudeBody, facilityImg).enqueue(new Callback<ModelResultAddFacilityInfo>() {
                @Override
                public void onResponse(@NonNull Call<ModelResultAddFacilityInfo> call, @NonNull Response<ModelResultAddFacilityInfo> response) {
                    progressBarHandler.hide();

                    if(response.isSuccessful()) {
                        final ModelResultAddFacilityInfo modelResultAddFacilityInfo = response.body();

                        assert modelResultAddFacilityInfo != null;
                        if(modelResultAddFacilityInfo.getResult().isStatus()) {
                            String msg = modelResultAddFacilityInfo.getResult().getMessage();

                            Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT)
                                    .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            super.onDismissed(transientBottomBar, event);

                                            backToMain();
                                        }
                                    })
                                    .show();
                        }
                    }
                    else {
                        try {
                            JSONObject jsonObject = new JSONObject(response.errorBody().string());
                            String message;
                            if(jsonObject.has("result")) {
                                message = jsonObject.getJSONObject("result").getString("message");
                            }
                            else {

                                message = jsonObject.getString("message");
                            }
                            Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT).show();
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ModelResultAddFacilityInfo> call, @NonNull Throwable t) {
                    // TODO: 12/10/2017 SET FAILURE SCENARIO
                    progressBarHandler.hide();
                    Snackbar.make(getWindow().getDecorView().getRootView(), t.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void backToMain() {
        facilityNameEdtxt.setText("");
        facilityPhoneEdtxt.setText("");

        bitmapResult = null;
        imageWarningTxt.setVisibility(View.INVISIBLE);
        addFacilityPicImg.setVisibility(View.VISIBLE);
        facilityPicImg.setImageResource(android.R.color.transparent);
        facilityPicImg.setVisibility(View.GONE);
        clearFacilityPicImgButton.setVisibility(View.INVISIBLE);

        mainContainer.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);
    }
}
