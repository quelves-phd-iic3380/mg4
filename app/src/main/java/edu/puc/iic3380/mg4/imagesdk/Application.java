package edu.puc.iic3380.mg4.imagesdk;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;

import ly.img.android.ImgLySdk;


public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImgLySdk.init(this);

        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
    }


}
