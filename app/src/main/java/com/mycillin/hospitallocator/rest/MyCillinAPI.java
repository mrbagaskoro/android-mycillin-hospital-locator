package com.mycillin.hospitallocator.rest;

import com.mycillin.hospitallocator.rest.addFacilityInfo.ModelResultAddFacilityInfo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by mrbagaskoro on 11-Mar-18.
 */

public interface MyCillinAPI {

    @Multipart
    @POST("add_facility_information/")
    Call<ModelResultAddFacilityInfo> addFacilityInformation(@Part("facility_name") RequestBody facilityName,
                                                            @Part("address") RequestBody address,
                                                            @Part("phone_no") RequestBody phoneNo,
                                                            @Part("latitude") RequestBody latitude,
                                                            @Part("longitude") RequestBody longitude,
                                                            @Part MultipartBody.Part facilityPict);
}
