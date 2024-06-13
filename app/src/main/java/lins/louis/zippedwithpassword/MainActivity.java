package lins.louis.zippedwithpassword;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.model.enums.CompressionLevel;

import java.io.File;

/**
 * MainActivity class responsible for creating a password-protected zip file
 * from a user-selected directory.
 */
public class MainActivity extends AppCompatActivity {

    // Request code for permissions
    private static final int REQUEST_PERMISSIONS = 1;

    // Tag for logging
    private static final String TAG = "MainActivity";

    // UI elements for password input and selected directory URI
    private EditText etPassword;
    private Uri selectedDirectoryUri;

    // ActivityResultLauncher for directory picker intent
    private final ActivityResultLauncher<Intent> directoryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the URI of the selected directory
                    selectedDirectoryUri = result.getData().getData();
                    Log.d(TAG, "Selected directory: " + selectedDirectoryUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize password EditText
        etPassword = findViewById(R.id.et_password);

        // Initialize and set click listener for directory picker button
        Button pickDirectoryButton = findViewById(R.id.btn_pick_directory);
        pickDirectoryButton.setOnClickListener(v -> openDirectoryPicker());

        // Initialize and set click listener for zip creation button
        Button zipButton = findViewById(R.id.btn_zip);
        zipButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                // Check if directory is selected and password is entered
                if (selectedDirectoryUri != null && !etPassword.getText().toString().isEmpty()) {
                    new ZipTask().execute();
                } else {
                    Toast.makeText(MainActivity.this, "Please select a directory and enter a password", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions();
            }
        });
    }

    /**
     * Opens a directory picker to allow the user to select a directory.
     */
    private void openDirectoryPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        directoryPickerLauncher.launch(intent);
    }

    /**
     * Checks if the necessary permissions are granted.
     *
     * @return true if permissions are granted, false otherwise.
     */
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests the necessary permissions.
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ZipTask().execute();
            } else {
                Toast.makeText(this, "Permissions required to create zip file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask for creating the zip file in the background.
     */
    private class ZipTask extends AsyncTask<Void, Void, String> {

        // Progress dialog to show zipping progress
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Initialize and show the progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Zipping files...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Create the zip file in the background
            return createZip();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog and show the result as a toast
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a password-protected zip file from the selected directory.
     *
     * @return Result message indicating success or failure.
     */
    private String createZip() {
        // Get the file path from the selected URI
        String directoryPath = getPathFromUri(selectedDirectoryUri);
        if (directoryPath == null) {
            return "Failed to get directory path";
        }

        // Get the parent directory of the selected directory
        File selectedDir = new File(directoryPath);
        String parentDirPath = selectedDir.getParent();
        if (parentDirPath == null) {
            return "Failed to get parent directory";
        }

        // Create the zip file path in the parent directory
        String zipFileName = selectedDir.getName() + ".zip";
        String zipFilePath = parentDirPath + "/" + zipFileName;

        // Get the password from the EditText
        String password = etPassword.getText().toString();

        try {
            // Create the password-protected zip file
            createPasswordProtectedZip(directoryPath, zipFilePath, password);
            return "Zip file created successfully";
        } catch (ZipException e) {
            Log.e(TAG, "createZip: Failed to create zip file", e);
            return "Failed to create zip file: " + e.getMessage();
        }
    }

    /**
     * Creates a password-protected zip file.
     *
     * @param directoryPath Path of the directory to zip.
     * @param zipFilePath Path to save the zip file.
     * @param password Password for the zip file.
     * @throws ZipException if an error occurs during zipping.
     */
    private void createPasswordProtectedZip(String directoryPath, String zipFilePath, String password) throws ZipException {
        // Initialize the ZipFile object with the zip file path and password
        ZipFile zipFile = new ZipFile(zipFilePath, password.toCharArray());

        // Set zip parameters for compression and encryption
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        // Add the directory to the zip file
        File folderToZip = new File(directoryPath);
        if (folderToZip.exists() && folderToZip.canRead()) {
            zipFile.addFolder(folderToZip, zipParameters);
        } else {
            throw new ZipException("No read access for the input zip file");
        }
    }

    /**
     * Gets the file path from the URI.
     *
     * @param uri URI of the selected directory.
     * @return File path of the selected directory.
     */
    private String getPathFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        // Get the document ID from the URI
        String docId = DocumentsContract.getTreeDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String path = split[1];

        // Handle primary storage and other storage types
        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + path;
        } else {
            return "/storage/" + type + "/" + path;
        }
    }
}
