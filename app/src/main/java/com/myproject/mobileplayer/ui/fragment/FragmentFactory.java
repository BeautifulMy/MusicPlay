package com.myproject.mobileplayer.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2017/3/8.
 */

public class FragmentFactory {
  public static Fragment create(Bundle bundle){
      Fragment fragment = new MediaListFragment();
      fragment.setArguments(bundle);
      return fragment;
  }
}
