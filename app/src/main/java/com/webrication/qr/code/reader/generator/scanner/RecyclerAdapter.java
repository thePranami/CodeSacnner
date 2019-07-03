package com.webrication.qr.code.reader.generator.scanner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.ArrayList;

/**
 * Created by pc on 1/18/2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.HolderView>
{
     Context context;
      ArrayList<String> list;
    OnItemClickListener mItemClickListener;

    public RecyclerAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public HolderView onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view=LayoutInflater.from(context).inflate(R.layout.custom_recycler,parent,false);

        HolderView holderView=new HolderView(view);

        return holderView;
    }

    @Override
    public void onBindViewHolder(HolderView holder, int position)
    {
        String data=list.get(position);

        if (data.equalsIgnoreCase("share"))
       {
           holder.textView.setText("share");
           holder.image.setImageResource(R.drawable.share);
       }
       if (data.equalsIgnoreCase("contact"))
       {
           holder.textView.setText("contact");
           holder.image.setImageResource(R.drawable.contact);
       }
       if (data.equalsIgnoreCase("Tel"))
       {
           holder.textView.setText("call");
           holder.image.setImageResource(R.drawable.call);
       }
       if (data.equalsIgnoreCase("Url"))
       {
           holder.textView.setText("open");
           holder.image.setImageResource(R.drawable.browser);
       }
       if (data.equalsIgnoreCase("Email"))
       {
           holder.textView.setText("email");
           holder.image.setImageResource(R.drawable.email);
       }
       if (data.equalsIgnoreCase("event"))
       {
           holder.textView.setText("event");
           holder.image.setImageResource(R.drawable.event);
       }
       if (data.equalsIgnoreCase("map"))
       {
           holder.textView.setText("map");
           holder.image.setImageResource(R.drawable.location);
       }
       if (data.equalsIgnoreCase("web"))
       {
           holder.textView.setText("search");
           holder.image.setImageResource(R.drawable.web);
       }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class HolderView extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView image;
        TextView textView;

        public HolderView(View itemView) {
            super(itemView);
            image=(ImageView)itemView.findViewById(R.id.icon);
            textView=(TextView)itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
