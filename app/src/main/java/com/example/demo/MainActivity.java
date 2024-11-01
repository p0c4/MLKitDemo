package com.example.demo;

//import static android.os.Build.VERSION_CODES.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.io.IOException;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageLabeler defaultLabeler;
    private ImageLabeler customLabeler;
    private ImageLabeler currentLabeler;
    private TextView labelResultTextView;
    private Spinner modelSpinner;
    private ImageView selectedImageView; // ImageView to display the selected image
    private InputImage lastSelectedImage = null; // Store the last selected image


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        modelSpinner = findViewById(R.id.modelSpinner);
        labelResultTextView = findViewById(R.id.labelResultTextView);
        selectedImageView = findViewById(R.id.selectedImageView); // Find the ImageView
        Button selectImageButton = findViewById(R.id.selectImageButton);

        // Set up the model dropdown menu
        String[] modelOptions = {"Default Model", "Custom TFLite Model"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modelOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("model.tflite")
                        // or .setAbsoluteFilePath(absolute file path to model file)
                        // or .setUri(URI to model file)
                        .build();

        // Initialize the default and custom labelers
        defaultLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        // Create the CustomImageLabelerOptions correctly
        CustomImageLabelerOptions customOptions = new CustomImageLabelerOptions.Builder(localModel)// Make sure the model filename is correct
                .setConfidenceThreshold(0.5f)
                .build();
        customLabeler = ImageLabeling.getClient(customOptions);

        // Set up Spinner listener for selecting the model
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentLabeler = defaultLabeler;
                } else {
                    currentLabeler = customLabeler;
                }
                // Re-process the image if one is already selected
                if (lastSelectedImage != null) {
                    processImage(lastSelectedImage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentLabeler = defaultLabeler;
            }
        });

        // Set up button click to select an image
        selectImageButton.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Display the chosen image in the ImageView
            selectedImageView.setImageURI(imageUri);

            try {
                // Convert the image to InputImage format and store it
                lastSelectedImage = InputImage.fromFilePath(this, imageUri);
                processImage(lastSelectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processImage(InputImage image) {
        labelResultTextView.setText("Processing...");

        currentLabeler.process(image)
                .addOnSuccessListener(labels -> displayResults(labels))
                .addOnFailureListener(e -> labelResultTextView.setText("Error: " + e.getMessage()));
    }

    private void displayResults(List<ImageLabel> labels) {
        StringBuilder resultText = new StringBuilder();
        for (ImageLabel label : labels) {
            resultText.append("Category: ").append(label.getText())
                    .append(", Confidence: ").append(label.getConfidence())
                    .append("\n");
        }
        labelResultTextView.setText(resultText.toString());
    }
}




//public class MainActivity extends AppCompatActivity {
//
//    private static final int PICK_IMAGE_REQUEST = 1;
//    private ImageView imageView;
//    private TextView resultTextView;
//    private ImageLabeler labeler;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Button selectImageButton = findViewById(R.id.selectImageButton);
//        imageView = findViewById(R.id.selectedImageView);
//        resultTextView = findViewById(R.id.resultTextView);
//
//        CustomImageLabelerOptions options =
//                new CustomImageLabelerOptions.Builder("model.tflite")
//                        .setConfidenceThreshold(0.5f)
//                        .build();
//
//        // Initialize Image Labeler
//        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
//
//        selectImageButton.setOnClickListener(v -> openGallery());
//    }
//
//    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                imageView.setImageBitmap(bitmap);
//                classifyImage(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void classifyImage(Bitmap bitmap) {
//        InputImage image = InputImage.fromBitmap(bitmap, 0);
//        labeler.process(image)
//                .addOnSuccessListener(labels -> displayLabels(labels))
//                .addOnFailureListener(e -> resultTextView.setText("Error classifying image"));
//    }
//
//    private void displayLabels(List<ImageLabel> labels) {
//        StringBuilder categories = new StringBuilder("Categories:\n");
//        for (ImageLabel label : labels) {
//            categories.append(label.getText()).append(" - Confidence: ").append(label.getConfidence()).append("\n");
//        }
//        resultTextView.setText(categories.toString());
//    }
//}
