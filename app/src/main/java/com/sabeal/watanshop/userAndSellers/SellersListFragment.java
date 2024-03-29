package com.sabeal.watanshop.userAndSellers;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.sabeal.watanshop.R;
import com.sabeal.watanshop.modelsList.sellersModel;
import com.sabeal.watanshop.public_profile.FragmentPublic_Profile;
import com.sabeal.watanshop.userAndSellers.adapter.ItemSellersListAdapter;
import com.sabeal.watanshop.utills.Network.RestService;
import com.sabeal.watanshop.utills.SettingsMain;
import com.sabeal.watanshop.utills.UrlController;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellersListFragment extends Fragment {
    ItemSellersListAdapter itemSellersListAdapter;
    ArrayList<sellersModel> sellersModelArrayList = new ArrayList<>();
    RecyclerView sellersRecylerView;
    RestService restService;
    SettingsMain settingsMain;
    Context context;
    Button btn_loadMore;
    int next_page;
    EditText searchView;
    boolean has_next_page;
    ArrayList<sellersModel> tempList = new ArrayList<>();

    public SellersListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sellers_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sellersRecylerView = view.findViewById(R.id.sellersRecylerView);
        btn_loadMore = view.findViewById(R.id.btn_loadMore);

        searchView = view.findViewById(R.id.mSearch);
        searchView.requestFocus();


        sellersRecylerView.setHasFixedSize(true);
        sellersRecylerView.setNestedScrollingEnabled(false);

        final GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager.setOrientation(RecyclerView.VERTICAL);
        sellersRecylerView.setLayoutManager(MyLayoutManager);

        settingsMain = new SettingsMain(getContext());
        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());
        context = getContext();
        btn_loadMore.setOnClickListener(v -> adforest_loadMoreSellersList());

        adforest_getSellersList();
        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setEnabled(false);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void filterSearch(CharSequence s) {

        sellersModelArrayList.clear();
        if (s.length()==0){
            sellersModelArrayList.addAll(tempList);
            Collections.sort(sellersModelArrayList, new Comparator<sellersModel>() {
                @Override
                public int compare(sellersModel o1, sellersModel o2) {
                    return o1.getAuthour_name().toLowerCase().compareTo(o2.getAuthour_name().toLowerCase());
                }
            });
        }else{
            for (int i =0;i<tempList.size();i++){
                String a = s.toString();
                if (tempList.get(i).getAuthour_name().toLowerCase().contains(a.toLowerCase())){
                    sellersModelArrayList.add(tempList.get(i));
                }
            }
        }
        itemSellersListAdapter.notifyDataSetChanged();

    }

    private void adforest_loadMoreSellersList() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());


            //post Type Mehtod for get Bid Details
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("page_number", next_page);
            Call<ResponseBody> myCall = restService.getMoreSellersList(jsonObject, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info MoreSellers Respon", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                Log.d("info MoreSellers Data", "" + response.getJSONObject("data"));


                                if (response.getJSONObject("data").getJSONArray("authors").length() > 0) {
                                    adforest_initializeList(response.getJSONObject("data").getJSONArray("authors"));
                                    tempList.clear();
                                    tempList.addAll(sellersModelArrayList);
                                    itemSellersListAdapter.notifyDataSetChanged();

                                } else {
//                                    textViewEmptyData.setVisibility(View.VISIBLE);
//                                    textViewEmptyData.setText(response.getJSONObject("data").get("no_top_bidders").toString());
                                }

                                JSONObject jsonObject = response.getJSONObject("data").getJSONObject("pagination");
                                next_page = jsonObject.getInt("next_page");
                                has_next_page = jsonObject.getBoolean("has_next_page");
                                if (!has_next_page) {
                                    btn_loadMore.setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info MoreSellers Excptn", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info MoreSellers error", String.valueOf(t));
                        Log.d("info MoreSellers error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_getSellersList() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());


            //post Type Mehtod for get Bid Details
            Call<ResponseBody> myCall = restService.getSellersList(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info sellers Respon", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                Log.d("info sellers Data", "" + response.getJSONObject("data"));

                                getActivity().setTitle(response.getJSONObject("data").getString("page_title"));

                                if (response.getJSONObject("data").getJSONArray("authors").length() > 0) {
                                    sellersModelArrayList.clear();
                                    adforest_initializeList(response.getJSONObject("data").getJSONArray("authors"));
                                    itemSellersListAdapter = new ItemSellersListAdapter(context, sellersModelArrayList);
                                    sellersRecylerView.setAdapter(itemSellersListAdapter);
                                    tempList.clear();
                                    tempList.addAll(sellersModelArrayList);
                                    itemSellersListAdapter.setOnItemClickListener(sellersModel -> {
                                        FragmentPublic_Profile fragment = new FragmentPublic_Profile();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("user_id", Integer.toString(sellersModel.getAuthor_id()));
                                        bundle.putString("requestFrom", "sellers");
                                        fragment.setArguments(bundle);
                                        replaceFragment(fragment, "FragmentPublic_Profile");
                                    });

                                } else {
//                                    textViewEmptyData.setVisibility(View.VISIBLE);
//                                    textViewEmptyData.setText(response.getJSONObject("data").get("no_top_bidders").toString());
                                }

                                JSONObject jsonObject = response.getJSONObject("data").getJSONObject("pagination");
                                next_page = jsonObject.getInt("next_page");
                                has_next_page = jsonObject.getBoolean("has_next_page");
                                if (has_next_page) {
                                    btn_loadMore.setVisibility(View.VISIBLE);
                                    btn_loadMore.setText(response.getJSONObject("data").getString("load_more"));
                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info sellers Excptn ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info sellers error", String.valueOf(t));
                        Log.d("info sellers error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    private void adforest_initializeList(JSONArray jsonArray) {
        {
            for (int i = 0; i < jsonArray.length(); i++) {
                sellersModel sellersModel = new sellersModel();
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sellersModel.setAuthor_id(jsonObject.getInt("author_id"));
                    sellersModel.setAuthour_name(jsonObject.getString("author_name"));
                    sellersModel.setAuthor_img(jsonObject.getString("author_img"));
                    sellersModel.setAuthor_rating(jsonObject.getString("author_rating"));
                    sellersModel.setAuthor_social(jsonObject.getJSONObject("author_social"));
                    sellersModel.setAuthor_location(jsonObject.getString("author_address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sellersModelArrayList.add(sellersModel);
            }
        }
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }
}
