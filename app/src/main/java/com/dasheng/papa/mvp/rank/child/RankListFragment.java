package com.dasheng.papa.mvp.rank.child;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;

import com.dasheng.papa.R;
import com.dasheng.papa.adapter.RankAdapter;
import com.dasheng.papa.base.BaseFragment;
import com.dasheng.papa.base.OnItemClickListener;
import com.dasheng.papa.bean.ApiListResBean;
import com.dasheng.papa.bean.ResponseItemBean;
import com.dasheng.papa.databinding.FragmentRankListBinding;
import com.dasheng.papa.util.Constant;
import com.dasheng.papa.util.FragmentUserVisibleController;
import com.dasheng.papa.util.UrlUtils;
import com.dasheng.papa.widget.DividerItemDecoration;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import timber.log.Timber;

import static com.dasheng.papa.widget.DividerItemDecoration.VERTICAL_LIST;

public class RankListFragment extends BaseFragment<FragmentRankListBinding> implements RankContact.View {
    private RankAdapter rankAdapter;
    private boolean isLoading;
    private int mCurrentPage;
    private int mTotalPages;
    private RankPresenter rankPresenter;
    private int day_type = 1;
    private boolean isParentHasShowed;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initEvent();
    }

    private void initView() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        binding.recycler.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        rankAdapter = new RankAdapter();
        binding.recycler.setAdapter(rankAdapter);
        binding.recycler.addItemDecoration(new DividerItemDecoration(getActivity(), VERTICAL_LIST, 0));
        rankAdapter.setOnItemClickListener(new OnItemClickListener<ResponseItemBean>() {
            @Override
            public void onClick(ResponseItemBean apiBean, int position) {
                UrlUtils.jumpToArticleOrVideo(getActivity(), apiBean);
            }
        });
    }

    private void initEvent() {
        binding.recycler.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                if (isLoading) {
                    return;
                }
                isLoading = true;
                rankPresenter.refresh(day_type);
            }

            @Override
            public void onLoadMore() {
                if (isLoading) {
                    return;
                }

                isLoading = true;
                rankPresenter.loadMore(day_type, mCurrentPage + 1);
            }
        });
        /*binding.swipe.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                if (isLoading) {
                    return;
                }
                isLoading = true;
                binding.swipe.setDataFinish(false);
                rankPresenter.refresh(day_type);
            }

            @Override
            public void onLoadMore() {
                if (isLoading) {
                    return;
                }
                if (mCurrentPage >= mTotalPages) {
                    binding.swipe.setDataFinish(true);
                    binding.swipe.onFinishFreshAndLoad();
                    return;
                }
                isLoading = true;
                rankPresenter.loadMore(day_type, mCurrentPage + 1);
            }
        });*/
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        Timber.d("rank  onFragmentVisibleChange  isVisible : %b", isVisible);
    }

    @Override
    protected void onFragmentFirstVisible() {
        Timber.d("rank  onFragmentFirstVisible");
        day_type = getArguments().getInt(Constant.Intent_Extra.RANK_DAY_TYPE);
        rankPresenter = new RankPresenter(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //                binding.swipe.callFresh();
            }
        }, 300);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_rank_list;
    }

    @Override
    public void onShowLoading() {

    }

    @Override
    public void onLoadingDismiss() {

    }

    @Override
    public void onRefreshSuccess(ApiListResBean<ResponseItemBean> apiBean) {
        resetLoadingStatus();
        mCurrentPage = 1;
        mTotalPages = apiBean.getTotal();
        rankAdapter.addRankItem(apiBean.getRes(), true);
    }

    @Override
    public void onLoadMoreSuccess(ApiListResBean<ResponseItemBean> apiBean) {
        resetLoadingStatus();
        mCurrentPage++;
        mTotalPages = apiBean.getTotal();
        if (mCurrentPage >= mTotalPages) {
            binding.recycler.setNoMore(true);
        }
        rankAdapter.addRankItem(apiBean.getRes());
    }

    @Override
    public void onError(Throwable e) {
        resetLoadingStatus();
    }

    private void resetLoadingStatus() {
        binding.recycler.loadMoreComplete();
        binding.recycler.refreshComplete();
        isLoading = false;
    }

    /**
     * * viewpager+fragments中fragment再嵌套viewpager+fragments时，
     * 会导致子fragments中第一个fragment的setUserVisible在父fragment未显示时传入true值
     * 且在父fragment变为不可见状态时，子fragment不会回调setUserVisible，导致逻辑上的可见
     * 下面代码配合父fragment中onFragmentFirstVisible中代码可暂时解决第一个问题。
     * {@link FragmentUserVisibleController}是网上找到的解决方案，尝试一次失败后暂时放弃，
     * 等待日后细读代码，研究逻辑
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Timber.d("rank  setUserVisibleHint  isVisibleToUser : %b", isVisibleToUser);
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment.getUserVisibleHint()) {
            isParentHasShowed = true;
        }
        if (!isParentHasShowed || parentFragment == null) {
            isVisibleToUser = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}
