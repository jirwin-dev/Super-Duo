package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by josh on 11/15/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)

public class ScoresWidgetRemoteViewsService extends RemoteViewsService {

    public static String ACTION_DATA_UPDATED = "barqsoft.footballscores.app.ACTION_DATA_UPDATED";

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.MATCH_ID,
    };
    // these indices must match the projection
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null)
                    data.close();

                //Get the current date
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dayFormat.format(System.currentTimeMillis());

                //Get matches for the current date
                Uri matchesOnDateUri = DatabaseContract.scores_table.buildScoreWithDate();
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        matchesOnDateUri,
                        null,
                        DatabaseContract.scores_table.DATE_COL + "=",
                        new String[]{date},
                        null
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.scores_widget_list_item);

                views.setTextViewText(R.id.home_name, data.getString(COL_HOME));
                views.setTextViewText(R.id.away_name, data.getString(COL_AWAY));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(data.getString(COL_HOME)));
                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(data.getString(COL_HOME)));
                views.setTextViewText(R.id.score_textview, Utilies.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS)));
                views.setTextViewText(R.id.data_textview, data.getString(COL_MATCHTIME));

//				Uri weatherUri = DatabaseContract.WeatherEntry.buildWeatherLocationWithDate(
//						locationSetting,
//						dateInMillis);
//				fillInIntent.setData(weatherUri);
                Intent fillInIntent = new Intent(getApplicationContext(), MainActivity.class);
                views.setOnClickFillInIntent(R.id.scores_list, fillInIntent);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.scores_widget, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
