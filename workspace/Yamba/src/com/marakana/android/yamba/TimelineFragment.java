package com.marakana.android.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

public class TimelineFragment extends ListFragment
		implements ViewBinder, LoaderCallbacks<Cursor> {
	private static final String[] FROM = {
		StatusContract.Columns.USER,
		StatusContract.Columns.MESSAGE,
		StatusContract.Columns.CREATED_AT
	};
	private static final int[] TO = {
		R.id.status_user,
		R.id.status_msg,
		R.id.status_date
	};
	
	private static final int TIMELINE_LOADER = 1;
	private LoaderManager mLoaderManager;
	private SimpleCursorAdapter mAdapter;
	
	private TimelineReceiver mReceiver;
	private IntentFilter mFilter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mLoaderManager = getLoaderManager();
		mLoaderManager.initLoader(TIMELINE_LOADER, null, this);
		
		// Create the adapter and install the ViewBinder.
		mAdapter = new SimpleCursorAdapter(getActivity(),
										   R.layout.timeline_row,
										   null, FROM, TO, 0);
		mAdapter.setViewBinder(this);
		setListAdapter(mAdapter);
		
		mReceiver = new TimelineReceiver();
		mFilter = new IntentFilter(YambaApplication.ACTION_NEW_STATUS);
	}

	@Override
	public void onStart() {
		super.onStart();
		mLoaderManager.restartLoader(TIMELINE_LOADER, null, this);
		getActivity().registerReceiver(mReceiver, mFilter, YambaApplication.PERM_RECEIVE_NEW_STATUS, null);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mReceiver);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		int id = view.getId();
		switch (id) {
		case R.id.status_date:
			// Create a user-friendly formatted date string
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
			TextView tv = (TextView) view;
			tv.setText(relTime);
			return true;
		default:
			return false;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		return new CursorLoader(getActivity().getApplicationContext(),
						 StatusContract.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private class TimelineReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// There are new status messages in the database/content provider.
			mLoaderManager.restartLoader(TIMELINE_LOADER, null, TimelineFragment.this);
		}
		
	}

}
