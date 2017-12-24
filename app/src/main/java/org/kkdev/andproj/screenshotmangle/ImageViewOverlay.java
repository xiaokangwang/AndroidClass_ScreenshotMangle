package org.kkdev.andproj.screenshotmangle;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by shelikhoo on 12/3/17.
 */

public class ImageViewOverlay extends ConstraintLayout {
    public ImageViewOverlay(Context context) {
        super(context);
        init();
    }

    public ImageViewOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.imagedispoverlay, this);

        view.findViewById(R.id.ShareTextView).setOnClickListener(v -> sendShareIntent());

        view.findViewById(R.id.btnShowText).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DispTextVIewer();
            }
        });
    }

    public void setShareText(String text) {
        this.sharingText = text;
    }
    public void setElementName(String text) {
        this.ElementName = text;
    }

    private void sendShareIntent() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+sharingText));
        getContext().startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void DispTextVIewer() {
        Intent ViewText = new Intent(this.getContext(),ViewTextActivity.class);
        ViewText.putExtra("Target", ElementName);
        this.getContext().startActivity(ViewText);
    }

    private String sharingText;
    private String ElementName;
}
