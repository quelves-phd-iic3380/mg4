package edu.puc.iic3380.mg4.imagesdk;

import ly.img.android.ImgLySdk;


public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImgLySdk.init(this);
    }


}
