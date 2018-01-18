package com.yangmo.mmwallpaper.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yangmo.mmwallpaper.R;
import com.yangmo.mmwallpaper.base.MyImageLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.WINDOW_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageFlowFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageFlowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFlowFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ImageFlowFragment() {
        // Required empty public constructor
    }

    private final int COLNUM = 2;   //列数

    private RecyclerView recyclerView;

    private ArrayList<String> imgList = new ArrayList<>();      //图片的url
    private ArrayList<Integer> heightList = new ArrayList<>();  //item高度
    private MyImageLoader imageLoader;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //为ImageView设置下载好的图片
            if (MyImageLoader.WHAT == msg.what) {
                MyImageLoader.ImageBean bean = (MyImageLoader.ImageBean) msg.obj;
                if (null != bean) {
                    imageLoader.addBitmapToCache(bean.url, bean.bitmap);
                    //防止图片错位
                    if (bean.url.equals(bean.imageView.getTag())) {
                        bean.imageView.setImageBitmap(bean.bitmap);
                    }
                }
            }
            //图片路径抓取完成，设置适配器
            else if (1 == msg.what) {
                recyclerView.setAdapter(new MyAdapter(imgList));
            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageFlowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFlowFragment newInstance(String param1, String param2) {
        ImageFlowFragment fragment = new ImageFlowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);

        imageLoader = new MyImageLoader(mHandler) {
            @Override
            public Bitmap loadBitmap(ImageBean bean) {
                Bitmap bitmap = null;
                try {
                    //从URL流读取图片
                    URL url = new URL(bean.url);
                    bitmap = BitmapFactory.decodeStream(url.openStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }
        };

        //抓取图片
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://image.baidu.com/search/index?tn=baiduimage&ct=201326592&lm=-1&cl=2&ie=gbk&word=%C3%C0%C5%AE&ala=1&fr=ala&alatpl=cover&pos=0#z=0&pn=&ic=0&st=-1&face=0&s=0&lm=-1")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();

                //用正则表达式匹配网页中图片的地址
                Pattern pattern = Pattern.compile("hoverURL\":\"[^\"]+");
                Matcher matcher = pattern.matcher(html);
                while (matcher.find()) {
                    String s = matcher.group();
                    imgList.add(s.substring(s.indexOf("http"), s.length()));
                }

                //匹配图片宽度
                pattern = Pattern.compile("\"width\":[0-9]+");
                matcher = pattern.matcher(html);
                String matcherStr = "\"width\":";
                int[] widths = new int[imgList.size()];
                int i = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    try {
                        int w = Integer.valueOf(s.substring(matcherStr.length(), s.length()));
                        widths[i++] = w;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //匹配图片高度
                pattern = Pattern.compile("\"height\":[0-9]+");
                matcher = pattern.matcher(html);
                matcherStr = "\"height\":";
                int[] heights = new int[imgList.size()];
                i = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    try {
                        int h = Integer.valueOf(s.substring(matcherStr.length(), s.length()));
                        heights[i++] = h;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //计算缩放item的实际高度
                int size = widths.length;
                double screenWidth = ((WindowManager) getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getWidth();
                double itemWidth = screenWidth / COLNUM;
                for (i = 0; i < size; i++) {
                    //计算宽高比例
                    double scale = heights[i] / (double) widths[i];
                    heightList.add((int) (scale * itemWidth));
                }

                mHandler.sendEmptyMessage(1);
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(COLNUM, StaggeredGridLayoutManager.VERTICAL));
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private ArrayList<String> dataList;

        public MyAdapter(ArrayList<String> dataList) {
            this.dataList = dataList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_image_layout, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            //设置图片大小
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = heightList.get(position);
            holder.itemView.setLayoutParams(params);

            holder.iv.setImageBitmap(null);
            //防止图片错位
            holder.iv.setTag(dataList.get(position));
            imageLoader.add(new MyImageLoader.ImageBean(holder.iv, dataList.get(position)));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView iv;

            public MyViewHolder(View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.iv);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_flow, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
