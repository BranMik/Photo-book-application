package com.branko.photobook;

import android.os.AsyncTask;
import android.widget.Toast;

public class ConcurentTasks {
	public static enum TaskType {
		IMPORT, EXPORT;
	}

	public static class ExportImportTask extends
			AsyncTask<Integer, Void, Boolean> {
		final TaskType taskType;
		final MainActivity mainAct;
		private String path;

		public ExportImportTask(TaskType task, MainActivity main,String path) {
			taskType = task;
			mainAct = main;
			this.path = path;
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			boolean bol = true;
			try {
				if (taskType == TaskType.EXPORT) {
					mainAct.exportBookToZip();
				} else {
					mainAct.unzipBookForImport(path);
				}
			} catch (Exception e) {
				bol = false;
			}
			return bol;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Boolean bol) {
			if (bol) {
				if (taskType == TaskType.IMPORT) {
					Toast.makeText(
							mainAct,
							mainAct.getResources().getString(
									R.string.import_finished), Toast.LENGTH_LONG)
							.show();
					mainAct.fillData();
				} else {
					Toast.makeText(
							mainAct,
							mainAct.getResources().getString(
									R.string.export_finished)
									+ path, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(mainAct,
						mainAct.getResources().getString(R.string.error),
						Toast.LENGTH_LONG).show();
			}

		}
	}
}
