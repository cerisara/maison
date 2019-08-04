package fr.xtof.maison;

import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.List;
import com.loopj.android.http.*;
import android.app.Dialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;
import java.util.Arrays;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import java.lang.Exception;
import java.net.URLEncoder;
import cz.msebera.android.httpclient.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import java.util.ArrayList;

public class Maison extends Activity {
	public static Context ctxt;
	public static Maison main;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ctxt=this;
		main=this;
		setContentView(R.layout.main);
        }

        public static boolean search(final File folder, List<String> result) {
            boolean res=true;
            File[] ff = folder.listFiles();
            if (ff==null) return false;
            for (final File f : ff) {
                if (f.isDirectory()) {
                    res=res&&search(f, result);
                }
                if (f.isFile()) result.add(f.getAbsolutePath());
            }
            return res;
        }

	private void msg(final String s) {
		main.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(main, s, Toast.LENGTH_LONG).show();
			}
		});
	}

	/** Called when the user touches the VIEW button */
	public void geturl(View view) {
		new AlertDialog.Builder(this)
			.setTitle("Upload tasks")
			.setMessage("Do you really want to upload this file ?")
			.setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DetProgressTask dett = new DetProgressTask(0,"");
                                dett.execute();
                            }})
                .setNegativeButton(android.R.string.no, null).show();
        }
        public void putfile(View view) {
            final String basedir = "/sdcard/maison";
            File dir = new File(basedir);
            if (!dir.exists()) {
                msg("no such dir: "+basedir);
                return;
            }
            ArrayList<String> results = new ArrayList<String>();
            if (!search(dir,results)) {
                msg("ERROR to access directory ?!");
                return;
            }
            if (results.size()==0) msg("found 0 file in "+basedir);
            else msg("found "+results.get(0));

            for (String fich: results) {
                msg("uploading "+fich+" ...");
                upload(fich);
            }
            /*
               try {
               s=URLEncoder.encode(s,"UTF-8");
               DetProgressTask dett = new DetProgressTask(1,s);
               dett.execute();
               } catch (Exception e) {
               e.printStackTrace();
               }
               */
        }

        private Thread uploader = null;
        private ArrayBlockingQueue<String> toupload = new ArrayBlockingQueue<String>(100);

        private void upload(final String f) {
            if (uploader==null) {
                uploader = new Thread(new Runnable() {
                    public void run() {
                        for (;;) {
                            try {
                                String ff = toupload.take();
                                if (ff=="END") break;
                                Socket s = new Socket("cerisara.duckdns.org",38634);
                                OutputStream output = s.getOutputStream();
                                {
                                    byte[] nom = ff.getBytes(Charset.forName("UTF-8"));
                                    int nomlen = nom.length;
                                    ByteBuffer b = ByteBuffer.allocate(4);
                                    //b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
                                    b.putInt(nomlen);
                                    byte[] nomlenb = b.array();
                                    output.write(nomlenb);
                                    output.write(nom);
                                }
                                byte[] data = new byte[1024];
                                FileInputStream fin = new FileInputStream(ff);
                                for (;;) {
                                    int nread=fin.read(data);
                                    if (nread<0) break;
                                    output.write(data,0,nread);
                                }
                                output.close();
                                if (s!=null) s.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        uploader=null;
                    }
                });
                uploader.start();
            }
            try {
                toupload.put(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	private class DetProgressTask extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog dialog = new ProgressDialog(ctxt);
		int getOuPut=0; // GET par defaut
		String url;

		public DetProgressTask(int getput, String url) {
			this.getOuPut=getput;
			this.url=url;
		}

		private void connect(final int typ, String url) {
			SyncHttpClient client = new SyncHttpClient();
			AsyncHttpResponseHandler rephdl = new AsyncHttpResponseHandler() {
				@Override
				public void onStart() {
					// called before request is started
					System.out.println("ON START");
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] response) {
					// called when response HTTP status is "200 OK"
					String str="OOO";
					try {
						str = new String(response, "UTF-8"); // for UTF-8 encoding
						System.out.println("RESPONSEEEEEE "+str);
						// encode
						//
						// decode
						// TODO parse the JSON
						// TODO handle priorities
						// TODO manage conflicts
						int i=str.indexOf("content\"");
						if (i>=0 && typ==0) {
							i+=10;
							int j=str.indexOf("\"",i);
							byte[] tmp2 = Base64.decode(str.substring(i,j),Base64.DEFAULT);
							str = new String(tmp2, "UTF-8");
							main.msg("Pull OK");
						} else if (typ==1) {
							main.msg("Push OK");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("ON SUCCESS "+str);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
					// called when response HTTP status is "4XX" (eg. 401, 403, 404)
					String str="OOO";
					try {
						str = new String(errorResponse, "UTF-8"); // for UTF-8 encoding
					} catch (Exception ee) {
						ee.printStackTrace();
					}
					System.out.println("ON FAILURE "+str);
				}

				@Override
				public void onRetry(int retryNo) {
					// called when request is retried
					System.out.println("ON RETRY");
				}
			};

                        /*
			if (typ==1) 
			else 
                        */
		}

		/** progress dialog to show user that the backup is processing. */

		/** application context. */

		protected void onPreExecute() {
			this.dialog.setMessage("Please wait");
			this.dialog.show();
		}

		protected Boolean doInBackground(final String... args) {
			try {
				connect(getOuPut,url);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (success) {
			} else {
				main.msg("Error postexec");
			}
		}
	}

}
