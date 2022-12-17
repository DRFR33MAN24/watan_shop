package com.sabeal.watanshop.helper;

import android.view.View;

import com.sabeal.watanshop.modelsList.catSubCatlistModel;

public interface CatSubCatOnclicklinstener {
    void onItemClick(catSubCatlistModel item);

    void onItemTouch(catSubCatlistModel item);

    void addToFavClick(View v, String position);

}
