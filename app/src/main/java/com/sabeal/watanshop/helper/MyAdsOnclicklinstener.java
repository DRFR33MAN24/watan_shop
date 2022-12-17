package com.sabeal.watanshop.helper;

import android.view.View;

import com.sabeal.watanshop.modelsList.myAdsModel;

public interface MyAdsOnclicklinstener {

    void onItemClick(myAdsModel item);

    void delViewOnClick(View v, int position);

    void editViewOnClick(View v, int position);

}
