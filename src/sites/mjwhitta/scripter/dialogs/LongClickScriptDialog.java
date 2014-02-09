/**
 * GNU GENERAL PUBLIC LICENSE See the file license.txt for copying conditions.
 * 
 * @author mjwhitta
 * @created Aug 16, 2012
 */
package sites.mjwhitta.scripter.dialogs;

import sites.mjwhitta.scripter.R;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;

/**
 * The dialog created when the user long clicks a script.
 */
public abstract class LongClickScriptDialog extends Dialog {
   /**
    * Constructor
    * 
    * @param activity
    */
   public LongClickScriptDialog(final Activity activity) {
      super(activity);

      setContentView(R.layout.long_click_script_dialog);
      setTitle("Make a selection");

      final Button configureButton = (Button) findViewById(R.id.script_edit_button);
      configureButton.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            onEditButton();
         }
      });

      final Button deleteButton = (Button) findViewById(R.id.script_delete_button);
      deleteButton.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            onDeleteButton();
         }
      });

      final Button cancelButton = (Button) findViewById(R.id.script_cancel_button);
      cancelButton.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            onCancelButton();
         }
      });
   }

   /**
    * Actions to take when the Cancel button is clicked
    */
   public abstract void onCancelButton();

   /**
    * Actions to take when the Delete button is clicked
    */
   public abstract void onDeleteButton();

   /**
    * Actions to take when the Configure button is clicked
    */
   public abstract void onEditButton();
}
