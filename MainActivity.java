package slidenerd.vivz.rotationfriendlytask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends ActionBarActivity implements
		OnItemClickListener {

	EditText selectionText;
	ListView chooseImagesList;
	String[] listOfImages;
	ProgressBar downloadImagesProgress;
	PlaceholderFragment placeholderFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		selectionText = (EditText) findViewById(R.id.urlSelectionText);
		chooseImagesList = (ListView) findViewById(R.id.chooseImageList);
		listOfImages = getResources().getStringArray(R.array.imageUrls);
		downloadImagesProgress = (ProgressBar) findViewById(R.id.downloadProgress);

		chooseImagesList.setOnItemClickListener(this);
		if (savedInstanceState == null) {
			placeholderFragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction()
					.add(placeholderFragment, "TaskFragment").commit();

		} else {
			placeholderFragment = (PlaceholderFragment) getSupportFragmentManager()
					.findFragmentByTag("TaskFragment");
		}
		if (placeholderFragment != null) {
			if (placeholderFragment.task != null
					&& placeholderFragment.task.getStatus() == AsyncTask.Status.RUNNING) {
				L.m("visible " + placeholderFragment.task);
				downloadImagesProgress.setVisibility(View.VISIBLE);

			} else {
				L.m("gone " + placeholderFragment.task);
				downloadImagesProgress.setVisibility(View.GONE);
			}
		}

	}

	public void downloadImage(View view) {
		if (selectionText.getText().toString() != null
				&& selectionText.getText().toString().length() > 0) {
			placeholderFragment.beginTask(selectionText.getText().toString());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateProgress(int progress) {
		downloadImagesProgress.setProgress(progress);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectionText.setText(listOfImages[position]);

	}

	public void showProgressBarWhileDownloading() {
		if (placeholderFragment.task != null) {
			if (downloadImagesProgress.getVisibility() != View.VISIBLE
					&& downloadImagesProgress.getProgress() != downloadImagesProgress
							.getMax()) {
				downloadImagesProgress.setVisibility(View.VISIBLE);
			}
		}
	}

	public void hideProgressBarAfterDownloading() {
		if (placeholderFragment.task != null) {
			if (downloadImagesProgress.getVisibility() == View.VISIBLE) {
				downloadImagesProgress.setVisibility(View.GONE);
			}
		}
	}
}

class PlaceholderFragment extends Fragment {
	RotationFriendlyTask task = null;
	Activity activity = null;
	int calculatedProgress = 0;
	int contentLength = 1;

	public PlaceholderFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.activity = activity;
		if (task != null) {
			task.onAttach(activity);
		}
	}

	public void beginTask(String url) {
		task = new RotationFriendlyTask(activity);
		task.execute(url);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if (task != null) {
			task.onDetach();
		}

	}

	public class RotationFriendlyTask extends
			AsyncTask<String, Integer, Boolean> {
		private Activity activity = null;

		public RotationFriendlyTask(Activity activity) {
			onAttach(activity);
		}

		private void onAttach(Activity activity) {
			this.activity = activity;

		}

		private void onDetach() {
			activity = null;

		}

		@Override
		protected void onPreExecute() {
			if (activity == null) {
				L.m("Error inside onPreExecute");
			} else {
				((MainActivity) activity).showProgressBarWhileDownloading();
			}

		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			/*
			 * 1 create the url object that represents the url 2 open connection
			 * using that url object 3 read data using input stream into a byte
			 * array 4 open a file output stream to save data on sd card 5 write
			 * data to the fileoutputstream 6 close the connections
			 */
			boolean successful = false;
			URL downloadURL = null;
			HttpURLConnection connection = null;
			InputStream inputStream = null;
			FileOutputStream fileOutputStream = null;
			File file = null;
			try {

				downloadURL = new URL(params[0]);
				connection = (HttpURLConnection) downloadURL.openConnection();
				contentLength = connection.getContentLength();
				L.m("content length " + contentLength);
				int count = 0;
				inputStream = connection.getInputStream();

				file = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES).getAbsolutePath()
						+ "/" + Uri.parse(params[0]).getLastPathSegment());
				fileOutputStream = new FileOutputStream(file);
				L.m("" + file.getAbsolutePath());
				int read = -1;
				byte[] buffer = new byte[1024];
				while ((read = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, read);
					count += read;
					publishProgress(count);
				}
				successful = true;
			} catch (MalformedURLException e) {
				L.m(e + "");

			} catch (IOException e) {
				L.m(e + "");

			} finally {

				if (connection != null) {
					connection.disconnect();
				}
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						L.m(e + "");

					}
				}
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						L.m(e + "");

					}
				}
			}
			return successful;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			calculatedProgress = (int) (((double) values[0] / contentLength) * 100);
			// L.m("" + values[0] + " " + contentLength + " " +
			// calculatedProgress);
			if (activity == null) {
				L.m("skipping onProgressUpdate since activity is null");
			} else {
				((MainActivity) activity).updateProgress(calculatedProgress);
			}

		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (activity == null) {
				L.m("error inside onPostExecute");
			} else {
				((MainActivity) activity).hideProgressBarAfterDownloading();
			}
		}

	}
}
