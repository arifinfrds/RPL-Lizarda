package com.lizarda.lizarda.ui.detail_produk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lizarda.lizarda.R;
import com.lizarda.lizarda.model.Comment;
import com.lizarda.lizarda.model.Model;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by arifinfrds on 11/1/17.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private ArrayList<Comment> mComments;
    private Context mContext;

    public CommentAdapter(ArrayList<Comment> comments, Context context) {
        this.mComments = comments;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_comment_detail_produk, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = mComments.get(position);

        holder.mTvUsername.setText(comment.getCommentOwnerId());
        holder.mTvComment.setText(comment.getText());

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;

        @BindView(R.id.iv_user_comment_detail_produk)
        CircleImageView mCivProfile;

        @BindView(R.id.tv_username_comment_detail_produk)
        TextView mTvUsername;

        @BindView(R.id.tv_detail_comment_detail_produk)
        TextView mTvComment;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            ButterKnife.bind(this, view);
        }
    }
}
