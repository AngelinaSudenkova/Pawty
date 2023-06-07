package com.example.pawty;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullImageFragment extends Fragment {

        private ImageView imageView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_full_image, container, false);

            imageView = view.findViewById(R.id.fullImageView);

            Bundle args = getArguments();
            if (args != null) {
                if (args.containsKey("imageRes")) {
                    int imageRes = args.getInt("imageRes");
                    imageView.setImageResource(imageRes);
                } else if (args.containsKey("imageUrl")) {
                    String imageUrl = args.getString("imageUrl");
                    Glide.with(requireContext()).load(imageUrl).into(imageView);
                }
            }

            return view;
        }
    }
