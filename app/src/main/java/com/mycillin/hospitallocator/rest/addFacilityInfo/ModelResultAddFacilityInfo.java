package com.mycillin.hospitallocator.rest.addFacilityInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mrbagaskoro on 11-Mar-18.
 */

public class ModelResultAddFacilityInfo {

    @SerializedName("result")
    @Expose
    private ModelResultDataAddFacilityInfo result;

    public ModelResultDataAddFacilityInfo getResult() {
        return result;
    }

    public void setResult(ModelResultDataAddFacilityInfo result) {
        this.result = result;
    }
}
