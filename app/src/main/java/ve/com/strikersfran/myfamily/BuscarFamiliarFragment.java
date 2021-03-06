package ve.com.strikersfran.myfamily;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.List;

import ve.com.strikersfran.myfamily.data.MiFamiliaBD;
import ve.com.strikersfran.myfamily.data.StaticConfig;
import ve.com.strikersfran.myfamily.model.ListMiFamilia;


/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarFamiliarFragment extends Fragment {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private ProgressBar mProgressBar;
    private FirebaseDatabase mDatabase;
    private List mItems;
    private ArrayList<String> listMiFamiliaID = null;
    private ListMiFamilia dataListMiFamilia = null;
    private Context context;
    private LovelyProgressDialog mProgresDialog;

    public BuscarFamiliarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myFragmentView = inflater.inflate(R.layout.fragment_buscar_familiar, container, false);
        context = myFragmentView.getContext();
        mProgresDialog = new LovelyProgressDialog(context);

        ImageButton btnBuscar = (ImageButton) myFragmentView.findViewById(R.id.fb_btn_buscar);
        final EditText txtFiltro = (EditText) myFragmentView.findViewById(R.id.fb_filtro);
        mProgressBar = (ProgressBar) myFragmentView.findViewById(R.id.fb_progress_bar) ;

        mDatabase = FirebaseDatabase.getInstance();

        if (dataListMiFamilia == null) {
            dataListMiFamilia = MiFamiliaBD.getInstance(getContext()).getListMiFamilia();
            listMiFamiliaID = new ArrayList<>();
            listMiFamiliaID.add(StaticConfig.UID);
            if (dataListMiFamilia.getListMiFamilia().size() > 0) {

                for (MiFamilia familia : dataListMiFamilia.getListMiFamilia()) {
                    listMiFamiliaID.add(familia.getUid());
                }
            }
        }

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filtro = txtFiltro.getText().toString().trim();

                if(TextUtils.isEmpty(filtro)){
                    txtFiltro.setError(getString(R.string.requerido));
                    txtFiltro.requestFocus();
                }
                else{
                    //mProgressBar.setVisibility(View.VISIBLE);
                    mProgresDialog.setCancelable(false)
                            .setTitle("Buscando Familiares....")
                            .setTopColorRes(R.color.primary_color)
                            .show();

                    mDatabase.getReference().child("users").orderByChild("nombre")
                            .startAt(filtro).endAt(filtro+"\\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //mProgressBar.setVisibility(View.GONE);
                            mProgresDialog.dismiss();

                            if (dataSnapshot.getValue() == null) {
                                new LovelyInfoDialog(context)
                                        .setTopColorRes(R.color.primary_color)
                                        .setTitle("Menudo detalle")
                                        .setMessage("No encontramos Familiares con esos datos")
                                        .show();

                            } else {
                                mItems = new ArrayList();
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                    String uid = singleSnapshot.child("uid").getValue(String.class);

                                    //para evitar listar personas que ya son mis familiares
                                    if(!listMiFamiliaID.contains(uid)/*&&!uid.equals(StaticConfig.UID)*/) {
                                        User user = new User();
                                        user.setNombre(singleSnapshot.child("nombre").getValue(String.class));
                                        user.setPrimerApellido(singleSnapshot.child("primerApellido").getValue(String.class));
                                        user.setSegundoApellido(singleSnapshot.child("segundoApellido").getValue(String.class));
                                        user.setAvatar(singleSnapshot.child("avatar").getValue(String.class));
                                        user.setEmail(singleSnapshot.child("email").getValue(String.class));
                                        user.setUid(uid);
                                        user.setLastUpdate((Long) singleSnapshot.child("lastUpdate").getValue());

                                        mItems.add(user);
                                    }
                                }

                                if(mItems.size() == 0){

                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.primary_color)
                                            .setTitle("Menudo detalle")
                                            .setMessage("No encontramos Familiares con esos datos")
                                            .show();
                                }
                                else {
                                    // Obtener el Recycler
                                    recycler = (RecyclerView) myFragmentView.findViewById(R.id.rv_buscar_familiar);
                                    recycler.setHasFixedSize(true);

                                    // Usar un administrador para LinearLayout
                                    lManager = new LinearLayoutManager(getContext());
                                    recycler.setLayoutManager(lManager);

                                    // Crear un nuevo adaptador
                                    adapter = new BuscarFamiliarAdapter(mItems);
                                    recycler.setAdapter(adapter);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //mProgressBar.setVisibility(View.GONE);
                            mProgresDialog.dismiss();
                            new LovelyInfoDialog(context)
                                    .setTopColorRes(R.color.primary_color)
                                    .setTitle("Error")
                                    .setMessage("Algo no Salio bien al buscar los familiares "+databaseError.getMessage())
                                    .show();
                        }
                    });


                }
            }
        });
        return myFragmentView;
    }

    /*Funcion para buscar todos los usuarios registrados excluyendo los agregado con familia*/
    public void buscarUsuarios(){
        mDatabase.getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.GONE);

                if (dataSnapshot.getValue() == null) {
                    Log.e("BUSCAR FAMILIAR", "No se encontraron registros");

                } else {
                    mItems = new ArrayList();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String nombre = singleSnapshot.child("nombre").getValue(String.class);
                        String pApellido = singleSnapshot.child("primerApellido").getValue(String.class);
                        String sApellido = singleSnapshot.child("segundoApellido").getValue(String.class);
                        String avatar = singleSnapshot.child("avatar").getValue(String.class);
                        String email = singleSnapshot.child("email").getValue(String.class);
                        String uid = singleSnapshot.child("uid").getValue(String.class);
                        mItems.add(new User(nombre, pApellido, sApellido, avatar, email,uid));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Buscar Familiares");
    }
}
