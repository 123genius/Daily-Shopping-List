package genius.com.example.dailyshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import genius.com.example.dailyshoppinglist.Model.Data;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FloatingActionButton fab_btn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private TextView totalsumResult;

    // Global Varaible

    private String type;
    private int amount;
    private String note;
    private String post_key;
    private String mAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Shopping List");

        totalsumResult = findViewById(R.id.total_amount);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);

        mDatabase.keepSynced(true);

        recyclerView = findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // Total sum of Amount

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int totalamount = 0;

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);

                    totalamount += data.getAmount();

                    String sttotal = String.valueOf(totalamount + ".00");

                    totalsumResult.setText(sttotal);
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });


        fab_btn = findViewById(R.id.fab);

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDailoge();
            }
        });
    }

    private void customDailoge()
    {
       // AlertDialog.Builder mydailog = new AlertDialog.Builder(HomeActivity.this);

        AlertDialog.Builder mydailog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myView = inflater.inflate(R.layout.input_data , null);
        final AlertDialog dialog = mydailog.create();
        dialog.setView(myView);

        final EditText type = myView.findViewById(R.id.edit_type);
        final EditText amount = myView.findViewById(R.id.edit_amount);
        final EditText note = myView.findViewById(R.id.edit_note);
        Button btnSave = myView.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();


                int ammount = Integer.parseInt(mAmount);

                if (TextUtils.isEmpty(mType))
                {
                    type.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(mAmount))
                {
                    amount.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(mNote))
                {
                    note.setError("Required Field...");
                    return;
                }

                String id = mDatabase.push().getKey();

                String date = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(mType , ammount , mNote , date , id);
                mDatabase.child(id).setValue(data);

                Toast.makeText(getApplicationContext(), "Data Add", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data , MyViewHolder>adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class,
                        R.layout.item_data,
                        MyViewHolder.class,
                        mDatabase
                )
        {

            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {

                viewHolder.setDate(model.getDate());
                viewHolder.setNote(model.getNote());
                viewHolder.setType(model.getType());
                viewHolder.setAmount(model.getAmount());

                viewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(position).getKey();
                        type = model.getType();
                        amount = model.getAmount();
                        note = model.getNote();

                        updateData();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        View myView;

        public MyViewHolder(View itemView)
        {
            super(itemView);
            myView = itemView;
        }

        public void setType(String type)
        {
            TextView mType = myView.findViewById(R.id.type);
            mType.setText(type);
        }

        public void setNote(String note)
        {
            TextView mNote = myView.findViewById(R.id.note);
            mNote.setText(note);
        }

        public void setDate(String date)
        {
            TextView mDate = myView.findViewById(R.id.date);
            mDate.setText(date);
        }

        public void setAmount(int amount)
        {
            TextView mAmount = myView.findViewById(R.id.amount);
            String stam = String.valueOf(amount);
            mAmount.setText(stam);
        }
    }

    public void updateData()
    {

        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);

        View myView = inflater.inflate(R.layout.update_inputfield , null);

        final AlertDialog dialog = mydialog.create();

        dialog.setView(myView);

        final EditText edt_Type = myView.findViewById(R.id.edit_type_upd);
        final EditText edt_Amount = myView.findViewById(R.id.edit_amount_upd);
        final EditText edt_Note = myView.findViewById(R.id.edit_note_upd);

        edt_Type.setText(type);
        edt_Type.setSelection(type.length());

        edt_Note.setText(note);
        edt_Note.setSelection(note.length());

        edt_Amount.setText(String.valueOf(amount));
        edt_Amount.setSelection(String.valueOf(amount).length());

        Button btnUpdate = myView.findViewById(R.id.btn_save_upd);
        Button btnDelete = myView.findViewById(R.id.btn_delete_upd);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               type = edt_Type.getText().toString().trim();
               mAmount = String.valueOf(amount);
               mAmount = edt_Amount.getText().toString().trim();
               note = edt_Note.getText().toString().trim();

               int intamount = Integer.parseInt(mAmount);

               String date = DateFormat.getDateInstance().format(new Date());
               Data data = new Data(type , intamount,note , date , post_key);
               mDatabase.child(post_key).setValue(data);

               dialog.dismiss();

            }
        });

            btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             mDatabase.child(post_key).removeValue();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext() , MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
