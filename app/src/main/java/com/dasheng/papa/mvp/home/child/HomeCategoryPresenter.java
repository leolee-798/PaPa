package com.dasheng.papa.mvp.home.child;

import android.support.annotation.NonNull;

import com.dasheng.papa.api.ApiFactory;
import com.dasheng.papa.api.BaseSubscriber;
import com.dasheng.papa.bean.ApiListResBean;
import com.dasheng.papa.bean.ResponseItemBean;
import com.dasheng.papa.util.Constant;
import com.dasheng.papa.util.GsonUtil;

import rx.Observable;
import rx.functions.Func2;
import timber.log.Timber;

public class HomeCategoryPresenter {
    private HomeCategoryContact.View mView;
    private HomeCategoryContact.Model mModel;

    public HomeCategoryPresenter(HomeCategoryContact.View view) {
        mView = view;
        mModel = initModel();
    }

    public void refresh(String type_id) {
        Observable.zip(mModel.refresh(type_id, "3"),
                mModel.refresh(type_id, "1"),
                new Func2<ApiListResBean<ResponseItemBean>, ApiListResBean<ResponseItemBean>,
                        ApiListResBean<ResponseItemBean>>() {
                    @Override
                    public ApiListResBean<ResponseItemBean> call(ApiListResBean<ResponseItemBean> banner,
                                                                 ApiListResBean<ResponseItemBean> apiBean) {
                        Timber.d("banner: %s \n apiBean: %s", GsonUtil.GsonString(banner), GsonUtil.GsonString
                                (apiBean));
                        apiBean.setBanner(banner.getRes());
                        return apiBean;
                    }
                }).subscribe(new BaseSubscriber<ApiListResBean<ResponseItemBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mView.onError(e);
            }

            @Override
            public void onNext(ApiListResBean<ResponseItemBean> apiBean) {
                mView.onRefresh(apiBean);
            }
        });
    }

    public void loadMore(String type_id, String page) {
        mModel.loadMore(type_id, page)
                .subscribe(new BaseSubscriber<ApiListResBean<ResponseItemBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.onError(e);
                    }

                    @Override
                    public void onNext(ApiListResBean<ResponseItemBean> apiBean) {
                        mView.onLoadMoreSuccess(apiBean);
                    }
                });
    }


    @NonNull
    private HomeCategoryContact.Model initModel() {
        return new HomeCategoryContact.Model() {
            @Override
            public Observable<ApiListResBean<ResponseItemBean>> refresh(String type_id, String status) {
                return ApiFactory.get_Content(type_id, status, null, "0", "4", mView);
            }

            @Override
            public Observable<ApiListResBean<ResponseItemBean>> loadMore(String type_id, String page) {
                return ApiFactory.get_Content(type_id, null, null, page, Constant.Api.SINGLE_PAGE_SIZE, mView);
            }
        };
    }

}
