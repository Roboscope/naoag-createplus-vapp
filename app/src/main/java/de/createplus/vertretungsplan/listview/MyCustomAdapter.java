package de.createplus.vertretungsplan.listview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

import de.createplus.vertretungsplan.R;

public class MyCustomAdapter extends BaseExpandableListAdapter {


    private LayoutInflater inflater;
    private ArrayList<Parent> mParent;

    public MyCustomAdapter(Context context, ArrayList<Parent> parent){
        mParent = parent;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getGroupCount() {
        return mParent.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mParent.get(i).getArrayChildren().size();
    }

    @Override
    public Object getGroup(int i) {
        return mParent.get(i).getTitle();
    }

    @Override
    public Object getChild(int i, int i1) {
        return mParent.get(i).getArrayChildren().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {

        ViewHolder holder = new ViewHolder();
        holder.groupPosition = groupPosition;

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_parent, viewGroup,false);
        }

        TextView textView = (TextView) view.findViewById(R.id.list_item_text_view);
        textView.setText(getGroup(groupPosition).toString());

        view.setTag(holder);
        return view;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,View view, ViewGroup viewGroup) {

        ViewHolder holder = new ViewHolder();
        holder.childPosition = childPosition;
        holder.groupPosition = groupPosition;

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_child, viewGroup,false);
        }

        TextView textView = (TextView) view.findViewById(R.id.list_item_text_child);
        String splitpoint =  " SPLITPOINT ";
        String[] text = mParent.get(groupPosition).getArrayChildren().get(childPosition).replace("&nbsp;","").split(splitpoint);
        textView.setText(text[0]);

        String Zusatz = text[1];
        //Log.e("Vertretungplan", "|"+Zusatz+"|");

        //ZWEITES LEERZEICHEN IST BESONDERS :O
        if(Zusatz.replace(" ","").replace(" ","").equals("")){Zusatz = "Keine weiteren Informationen vorhanden!";}
        final String ZusatzAG = Zusatz;

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(v.getContext());
                dlgAlert.setMessage(ZusatzAG);
                dlgAlert.setTitle("Infos:");
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }
        });

            view.setTag(holder);

        //return the entire view
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        /* used to make the notifyDataSetChanged() method work */
        super.registerDataSetObserver(observer);
    }


    protected class ViewHolder {
        protected int childPosition;
        protected int groupPosition;
        protected Button button;
    }
}