package sheyko.aleksey.restsurvey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sheyko.aleksey.restsurvey.PageFragment.OnAnswerSelectedListener;
import sheyko.aleksey.restsurvey.provider.QuestionDataSource;


public class SurveyActivity extends FragmentActivity
        implements OnAnswerSelectedListener {

    private static final String TAG = StartActivity.class.getSimpleName();

    private ViewPager mPager;
    private QuestionDataSource mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        try {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "cvwSNlSuCvUWvOP9RYXtPhWZR3Bm69xgT979VZk3",
                    "S72yDeO7sVS96p9IRjZzmeE9sy6WwxVhZsdn2sFQ");
            ParseAnalytics.trackAppOpenedInBackground(getIntent());
        } catch (IllegalStateException ise) {
            Log.i(TAG, "Parse already initialized");
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(
                getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);

        ParseObject session = new ParseObject("Session");
        session.saveInBackground();
    }

    @Override public void onAnswerSelected() {
        if (mPager.getCurrentItem() < mDataSource.getCount() - 1) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        } else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Session");
            query.orderByDescending("createdAt");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override public void done(ParseObject session, ParseException e) {
                    if (e == null) {
                        Map<String, String> dimensions = new HashMap<>();

                        List<List<String>> answers = session.getList("answers");
                        for(List<String> answer : answers) {
                            dimensions.put(answer.get(0), answer.get(1));
                        }
                        // TODO: Uncomment before releasing
                        // ParseAnalytics.trackEventInBackground("Answers", dimensions);
                    }
                }
            });
            startActivity(new Intent(this, StartActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            mDataSource = new QuestionDataSource();
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mDataSource.getCount();
        }
    }
}
