package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.ui.adapter.PicturePagerAdapter;
import com.hiroshi.cimoc.utils.CustomFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class ReaderActivity extends Activity {

    @BindView(R.id.reader_view_pager) ViewPager mViewPager;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        ButterKnife.bind(this);
        String[] images = {
                "http://i.hamreus.com:8080/ps3/d/%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9/%E7%AC%AC1%E5%9B%9E/172807_1810.jpg",
                "http://i.hamreus.com:8080/ps3/d/%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9/%E7%AC%AC1%E5%9B%9E/172807_4385.jpg",
                "http://i.hamreus.com:8080/ps3/d/%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9/%E7%AC%AC1%E5%9B%9E/172807_6382.jpg",
                "http://i.hamreus.com:8080/ps3/d/%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9/%E7%AC%AC1%E5%9B%9E/172917_2734.jpg",
                "http://i.hamreus.com:8080/ps3/d/%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9/%E7%AC%AC1%E5%9B%9E/172917_4469.jpg"
        };
        mPagerAdapter = new PicturePagerAdapter(images, getLayoutInflater(), CustomFactory.getPicasso(this, Kami.SOURCE_IKANMAN));
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
    }

}
