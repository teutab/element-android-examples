package com.element.authentication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.element.orbit.Authentication;
import com.element.orbit.Config;
import com.element.orbit.Message;
import com.element.orbit.OrbitListener;
import com.element.orbit.OrbitService;
import com.element.orbit.RequestManager;
import com.element.orbit.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OrbitListener {

    private View confirm;

    private RequestManager requestManager;

    private MyRecyclerAdapter myRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confirm = findViewById(R.id.confirm);

        requestManager = new RequestManager(MainActivity.this);
        myRecyclerAdapter = new MyRecyclerAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.setAdapter(myRecyclerAdapter);

        OrbitService.registerCallback(this);

        Config config = new Config();
        config.isDirectAccountCreation = true;
        requestManager.updateConfig(config);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestManager.syncUserData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        confirm.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        OrbitService.unregisterCallback(this);
    }

    public void clickOnNewUser(View view) {
        requestManager.addNewUser();
    }

    public void clickOnTrainModel(View view) {
        requestManager.trainModel();
    }

    private void clickOnUserDataRow(UserData userData) {
        requestManager.authenticateUser(userData);
    }

    public void clickOnConfirmButtons(View view) {
        confirm.setVisibility(View.GONE);
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<UserData> userDataList = new ArrayList<>();

        void update(HashMap<String, UserData> userDataMap) {
            for (int i = 0; i < userDataList.size(); i++) {
                UserData userData = userDataList.get(i);
                String mapKey = userData.userId;
                if (userDataMap.containsKey(mapKey)) {
                    userDataList.set(i, userDataMap.get(mapKey));
                    userDataMap.remove(mapKey);
                }
            }
            userDataList.addAll(userDataMap.values());
            notifyDataSetChanged();
        }

        void addAll(List<UserData> newUserDataList) {
            userDataList.clear();
            userDataList.addAll(newUserDataList);
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_data_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
            final UserData userData = userDataList.get(position);

            myViewHolder.name.setText(userData.fullName());

            int statusCode = userData.getStatusCode();
            String status = OrbitService.getUserStatus(statusCode);
            if (TextUtils.isEmpty(status)) {
                myViewHolder.status.setVisibility(View.GONE);
            } else {
                myViewHolder.status.setVisibility(View.VISIBLE);
                myViewHolder.status.setText(status);
            }

            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickOnUserDataRow(userData);
                }
            });
            myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    requestManager.deleteUser(userData);
                    userDataList.remove(userData);
                    myRecyclerAdapter.notifyDataSetChanged();
                    toast("removed, " + userData.fullName());
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return userDataList.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView name;
        TextView status;

        MyViewHolder(View view) {
            super(view);
            itemView = view;
            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
        }
    }

    @Override
    public void onAllUserData(@NonNull List<UserData> userDataList) {
        myRecyclerAdapter.addAll(userDataList);
    }

    @Override
    public void onUpdatedUserData(@NonNull HashMap<String, UserData> userDataMap) {
        myRecyclerAdapter.update(userDataMap);
    }

    @Override
    public void onUpdatedConfig(@NonNull Config config) {
        String portal = getString(R.string.portal_package);
        toast(portal + " Ver: " + config.sdkVersion);
    }

    @Override
    public void onAuthenticated(@NonNull Authentication authentication) {
        int resultCode = authentication.resultCode;
        if (resultCode == Authentication.CODE_INVALID_PALM_SIZE || resultCode == Authentication.CODE_INTERNAL_ERROR) {
            toast(authentication.message);
            return;
        }

        confirm.setVisibility(View.VISIBLE);

        TextView confirmText = (TextView) findViewById(R.id.confirmText);
        ImageView confirmImage = (ImageView) findViewById(R.id.confirmImage);

        if (authentication.antispoofIsReal == 0) {
            confirmText.setText("Fake Authentication");
            confirmImage.setImageResource(R.drawable.icon_focus);
        } else if (resultCode == Authentication.CODE_VERIFIED) {
            confirmText.setText(authentication.message);
            confirmImage.setImageResource(R.drawable.icon_check);
        } else {
            confirmText.setText(authentication.message);
            confirmImage.setImageResource(R.drawable.icon_focus);
        }
    }

    @Override
    public void onMessage(@NonNull Message message) {
        toast(message.text);
    }

    private void toast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}