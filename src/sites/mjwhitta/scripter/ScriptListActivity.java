/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Aug 16, 2012
 */
package sites.mjwhitta.scripter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sites.mjwhitta.andlib.mjwhitta.activities.ListActivity;
import sites.mjwhitta.andlib.mjwhitta.dialogs.OutputDialog;
import sites.mjwhitta.andlib.mjwhitta.utils.CheckBoxList;
import sites.mjwhitta.andlib.mjwhitta.utils.Root;
import sites.mjwhitta.andlib.mjwhitta.utils.Shell;
import sites.mjwhitta.andlib.mjwhitta.utils.StringUtils;
import sites.mjwhitta.scripter.dialogs.LongClickScriptDialog;
import sites.mjwhitta.scripter.dialogs.NewScriptDialog;
import sites.mjwhitta.scripter.dialogs.SettingsDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ScriptListActivity extends ListActivity<Script> {
   protected class GetScriptsAsyncTask extends
         AsyncTask<Void, String, List<Script>> {
      protected final List<Script> m_scripts = new ArrayList<Script>();

      protected int m_numOperations = 0;

      protected int m_operation = 0;

      protected String m_message = "Loading scripts";

      /**
       * @see android.os.AsyncTask#doInBackground(Params[])
       */
      @Override
      protected List<Script> doInBackground(Void... params) {
         for (String scriptPath : m_scriptPaths.split(":")) {
            if (!isCancelled()) {
               File dir = new File(scriptPath);
               if (dir.exists()) {
                  if (dir.list() != null) {
                     for (String file : dir.list()) {
                        File f = new File(scriptPath + "/" + file);
                        if (f.isFile()) {
                           try {
                              publishProgress();
                              ++m_operation;
                              Script s = new Script(f);
                              String testIfScript = Shell.execute(
                                    "head " + s.getFullPath()).trim();
                              if (testIfScript.startsWith("#!/")) {
                                 m_scripts.add(s);
                              }
                           } catch (Exception e) {
                              publishProgress(f.getPath(), e.getMessage());
                           }
                        }
                     }
                  }
               }
            }
         }
         publishProgress();
         return m_scripts;
      }

      @Override
      protected void onPostExecute(List<Script> scripts) {
         m_list.removeAll();
         m_list.addAll(scripts);

         for (Script s : scripts) {
            if (!Arrays.asList(fileList()).contains(s.getConfigName())) {
               try {
                  s.saveAttributes(openFileOutput(s.getConfigName(),
                        MODE_PRIVATE));
               } catch (Exception e) {
                  // shouldn't happen b/c I'm not appending
               }
            } else {
               try {
                  s.restoreAttributes(openFileInput(s.getConfigName()));
               } catch (Exception e) {
                  toastError("Can't restore attributes b/c:\n\n"
                        + e.getMessage());
               }
            }
            m_list.setSelected(s, s.runAsRoot());
         }

         setLoading(false);
      }

      @Override
      protected void onPreExecute() {
         setLoading(true, m_message, 0);
         for (String scriptPath : m_scriptPaths.split(":")) {
            File dir = new File(scriptPath);
            if (dir.exists()) {
               if (dir.list() != null) {
                  for (String file : dir.list()) {
                     File f = new File(scriptPath + "/" + file);
                     if (f.isFile()) {
                        ++m_numOperations;
                     }
                  }
               }
            }
         }
         m_list.removeAll();
      }

      /**
       * @see android.os.AsyncTask#onProgressUpdate(Progress[])
       */
      @Override
      protected void onProgressUpdate(String... progress) {
         double percent = ((double) m_operation / (double) m_numOperations) * 100;
         setLoading(true, m_message, (int) percent);
         if (progress.length > 0) {
            toastError("Error reading contents of " + progress[0] + " b/c:\n\n"
                  + progress[1]);
         }
      }
   }

   protected String m_scriptPaths;

   protected ArrayAdapter<String> m_scriptPathsDropDownList;

   protected GetScriptsAsyncTask m_getScriptsTask;

   /**
    * Create a new Script and add it to the list
    */
   protected void addNewScript(String scriptPath, String newScriptName) {
      // Create Script
      Script s = new Script(new File(scriptPath + "/" + newScriptName));

      // Save default contents
      try {
         BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
               s.getFullPath())));
         bw.write("#!/system/bin/sh\n\necho \"Hello World!\"");
         Thread.sleep(500);
         bw.close();

         // Save new Script's attributes to file
         try {
            s.saveAttributes(openFileOutput(s.getConfigName(), MODE_PRIVATE));
         } catch (Exception e) {
            // shouldn't happen b/c I'm not appending
         }

         // Add new Script to the list and update the GUI
         m_list.add(s);

         // Set ScriptSingleton to be the new Script
         ScriptSingleton.setInstance(s);
         editScript();
      } catch (IOException e) {
         toastError("Failed to save default contents of file "
               + s.getFullPath() + " b/c:\n\n" + e.getMessage());
      } catch (InterruptedException e) {/* do nothing */}

      refreshList();
   }

   /**
    * Check to see if storage is readable or writable
    * 
    * @return A pair of Booleans, first is readable, second is writable
    */
   protected Pair<Boolean, Boolean> checkMedia() {
      boolean storageReadable = false;
      boolean storageWriteable = false;
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state)) {
         // We can read and write the media
         storageReadable = storageWriteable = true;
      } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
         // We can only read the media
         storageReadable = true;
         storageWriteable = false;
      } else {
         // Something else is wrong. It may be one of many other states, but
         // all we need to know is we can neither read nor write
         storageReadable = storageWriteable = false;
      }

      return new Pair<Boolean, Boolean>(storageReadable, storageWriteable);
   }

   /**
    * Edit the selected Script
    */
   protected void editScript() {
      startActivityForResult(new Intent(this, EditScriptActivity.class),
            R.id.save_button);
   }

   /**
    * @return This list of Scripts
    */
   public CheckBoxList<Script> getList() {
      return m_list;
   }

   /**
    * @return The path to the directory of Scripts
    */
   public String getScriptPaths() {
      return m_scriptPaths;
   }

   /**
    * Initialize everything
    */
   protected void init() {
      m_list = new CheckBoxList<Script>(this);

      // Get script path from file if it exists else use default
      if (Arrays.asList(fileList()).contains("script_paths")) {
         String scriptPath = Environment.getExternalStorageDirectory()
               .getPath();
         try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                  openFileInput("script_paths")));
            scriptPath = br.readLine().trim();
            try {
               br.close();
            } catch (IOException e) {
               // do nothing, path was probably restored
            }
         } catch (FileNotFoundException e) {
            // can't happen b/c I check first
         } catch (IOException e) {
            toastError("Using default script path! Couldn't restore script paths b/c:\n\n"
                  + e.getMessage());
         }

         updatedScriptPath(scriptPath);
      } else {
         updatedScriptPath(Environment.getExternalStorageDirectory().getPath());
      }
   }

   /**
    * @see sites.mjwhitta.andlib.mjwhitta.activities.ListActivity#listItemClick(int)
    */
   @Override
   public void listItemClick(int position) {
      // Fix check box
      Script s = m_list.getItem(position);
      m_list.setSelected(position, s.runAsRoot());
      ScriptSingleton.setInstance(s);
      if (s.hideOutput()) {
         String command = "sh " + s.getFullPath() + " " + s.getArgs();
         try {
            if (s.runAsRoot()) {
               Shell.executeAsRoot(command);
            } else {
               Shell.execute(command);
            }
            toastMessage("Script " + s.getName() + " ran successfully");
         } catch (Exception e) {
            toastError("Script " + s.getName() + " failed!");
         }
      } else {
         showDialog(OutputDialog.ID);
      }
   }

   /**
    * @see sites.mjwhitta.andlib.mjwhitta.activities.ListActivity#listItemLongClick(int)
    */
   @Override
   public void listItemLongClick(int position) {
      ScriptSingleton.setInstance(m_list.getItem(position));
      showDialog(R.id.script_edit_button);
   }

   /**
    * @see android.app.Activity#onActivityResult(int, int,
    *      android.content.Intent)
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (resultCode == RESULT_OK) {
         Script s = ScriptSingleton.getInstance();
         m_list.setSelected(s, s.runAsRoot());
         try {
            s.saveAttributes(openFileOutput(s.getConfigName(), MODE_PRIVATE));
         } catch (Exception e) {
            // shouldn't happen b/c I'm not appending
         }
      }

      super.onActivityResult(requestCode, resultCode, data);
   }

   /**
    * @see android.app.Activity#onCreate(android.os.Bundle)
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Root.requestRoot();
      init();
   }

   /**
    * @see android.app.Activity#onCreateDialog(int)
    */
   @Override
   protected Dialog onCreateDialog(int id) {
      if (id == R.id.menu_new_script) {
         return new NewScriptDialog(this, m_scriptPathsDropDownList) {
            @Override
            public void onOkButton(int parentDir, String newScriptName) {
               removeDialog(R.id.menu_new_script);
               addNewScript(m_scriptPaths.split(":")[parentDir], newScriptName);
            }
         };
      } else if (id == R.id.menu_settings) {
         return new SettingsDialog(this) {
            /**
             * @see sites.mjwhitta.scripter.dialogs.SettingsDialog#onOkButton(java.lang.String)
             */
            @Override
            public void onOkButton(String paths) {
               removeDialog(R.id.menu_settings);
               updatedScriptPath(paths);
            }
         };
      } else if (id == OutputDialog.ID) {
         return new OutputDialog(this, "Output:") {
            /**
             * @see sites.mjwhitta.scripter.dialogs.ScriptOutputDialog#getOutput()
             */
            @Override
            public String getOutput() {
               String output = "";
               Script s = ScriptSingleton.getInstance();

               try {
                  String command = "sh " + s.getFullPath() + " " + s.getArgs();
                  if (s.runAsRoot()) {
                     output = Shell.executeAsRoot(command);
                  } else {
                     output = Shell.execute(command);
                  }
               } catch (Exception e) {
                  output = "Error running script " + s + " b/c:\n\n"
                        + e.getMessage();
               }
               return output;
            }

            /**
             * @see sites.mjwhitta.scripter.dialogs.ScriptOutputDialog#onCloseButton()
             */
            @Override
            public void onCloseButton() {
               removeDialog(OutputDialog.ID);
            }
         };
      } else if (id == R.id.script_edit_button) {
         return new LongClickScriptDialog(this) {
            /**
             * @see sites.mjwhitta.scripter.dialogs.LongClickScriptDialog#onCancelButton()
             */
            @Override
            public void onCancelButton() {
               removeDialog(R.id.script_edit_button);
            }

            /**
             * @see sites.mjwhitta.scripter.dialogs.LongClickScriptDialog#onDeleteButton()
             */
            @Override
            public void onDeleteButton() {
               removeDialog(R.id.script_edit_button);
               removeScript();
            }

            /**
             * @see sites.mjwhitta.scripter.dialogs.LongClickScriptDialog#onEditButton()
             */
            @Override
            public void onEditButton() {
               removeDialog(R.id.script_edit_button);
               editScript();
            }
         };
      } else {
         return null;
      }
   }

   /**
    * @see com.actionbarsherlock.app.SherlockFragmentActivity#onCreateOptionsMenu(android.view.Menu)
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(0, R.id.menu_new_script, 0, "New Script").setShowAsAction(
            MenuItem.SHOW_AS_ACTION_IF_ROOM);
      menu.add(0, R.id.menu_refresh, 0, "Refresh").setShowAsAction(
            MenuItem.SHOW_AS_ACTION_IF_ROOM);
      menu.add(0, R.id.menu_settings, 0, "Settings").setShowAsAction(
            MenuItem.SHOW_AS_ACTION_NEVER);
      return true;
   }

   /**
    * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menu_new_script:
         if (checkMedia().second == true) {
            showDialog(R.id.menu_new_script);
         } else {
            toastError("SDCard is not writable!");
         }
         break;
      case R.id.menu_refresh:
         refreshList();
         break;
      case R.id.menu_settings:
         showDialog(R.id.menu_settings);
         break;
      default:
         toastError("Unexpected action value " + item.getItemId());
      }

      return true;
   }

   /**
    * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
    */
   @Override
   protected void onPrepareDialog(int id, Dialog dialog) {
      super.onPrepareDialog(id, dialog);
   }

   protected void refreshList() {
      if (m_getScriptsTask != null) {
         m_getScriptsTask.cancel(true);
      }
      m_getScriptsTask = new GetScriptsAsyncTask();
      m_getScriptsTask.execute();
   }

   /**
    * Deletes a script from the list
    */
   protected void removeScript() {
      Script s = ScriptSingleton.getInstance();

      // Delete Script
      new File(s.getFullPath()).delete();

      // Delete saved Script config file
      deleteFile(s.getConfigName());

      // Remove Script from the list and update the GUI
      m_list.remove(s);
      refreshList();
   }

   /**
    * @see sites.mjwhitta.andlib.mjwhitta.activities.ListActivity#setLoading(boolean)
    */
   @Override
   protected void setLoading(boolean loading) {
      setLoading(loading, "No scripts found", 101);
   }

   /**
    * @see sites.mjwhitta.andlib.mjwhitta.activities.ListActivity#setLoading(boolean,
    *      int)
    */
   @Override
   protected void setLoading(boolean loading, int percent) {
      setLoading(loading, "No scripts found", percent);
   }

   /**
    * Update list of scripts
    * 
    * @param paths
    */
   protected void updatedScriptPath(String paths) {
      if (checkMedia().first == true) {
         String[] splitPaths = paths.split(":");
         String newPaths = "";
         for (String path : splitPaths) {
            if ((!path.isEmpty() && path.substring(1).startsWith("sdcard"))
                  || path.startsWith(Environment.getExternalStorageDirectory()
                        .getPath()) || Root.isRoot()) {

               File dir = new File(path);
               if (!dir.exists()) {
                  toastError("Path " + path + " does not exist!");
               } else if (!dir.isDirectory()) {
                  toastError("Path " + path + " is not a directory!");
               } else {
                  if (StringUtils.isNullorEmpty(newPaths)) {
                     newPaths += path;
                  } else {
                     newPaths += ":" + path;
                  }
               }
            } else {
               toastError("Only scripts on your SDCard are supported unless you have root!");
            }
         }

         try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                  openFileOutput("script_paths", Context.MODE_PRIVATE)));
            bw.write(paths);
            bw.close();
         } catch (FileNotFoundException e) {
            // can't happen b/c I'm not appending
         } catch (IOException e) {
            toastError("Unknown error occurred b/c:\n\n" + e.getMessage());
         }

         // Save new paths
         if (!StringUtils.isNullorEmpty(newPaths)) {
            m_scriptPaths = newPaths;
         }

         if (StringUtils.isNullorEmpty(m_scriptPaths)) {
            m_scriptPaths = Environment.getExternalStorageDirectory().getPath();
         }

         // Set up array adapter for Cipher types
         m_scriptPathsDropDownList = new ArrayAdapter<String>(this,
               R.layout.parent_dir_list_item, m_scriptPaths.split(":")) {
            /**
             * @see android.widget.ArrayAdapter#getDropDownView(int,
             *      android.view.View, android.view.ViewGroup)
             */
            @Override
            public View getDropDownView(int position, View convertView,
                  ViewGroup parent) {
               View view = super.getView(position, convertView, parent);
               if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                  TextView text = (TextView) view
                        .findViewById(R.id.dropdown_list_item);
                  text.setBackgroundColor(Color.BLACK);
               }

               return view;
            }
         };
         m_scriptPathsDropDownList
               .setDropDownViewResource(R.layout.parent_dir_list_item);

         // Get Scripts
         refreshList();
      } else {
         toastError("SDCard is not readable!");

         finish();
      }
   }
}
