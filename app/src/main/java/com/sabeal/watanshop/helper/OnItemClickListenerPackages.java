package com.sabeal.watanshop.helper;

import com.sabeal.watanshop.modelsList.PackagesModel;

public interface OnItemClickListenerPackages {
    void onItemClick(PackagesModel item);

    void onItemTouch();

    void onItemSelected(PackagesModel packagesModel, int spinnerPosition);
}
