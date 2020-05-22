package com.example.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMenbrosSelecionados,recyclerMenbros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMenbros = new ArrayList<>();
    private List<Usuario> listaMenbrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMenbros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    public void atualizarMenbrosToolbar(){

        int totalSelecionados = listaMenbrosSelecionados.size();
        int total = listaMenbros.size() + totalSelecionados;
        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");

        setSupportActionBar(toolbar);

        //Configurações Iniciais
        recyclerMenbros = findViewById(R.id.recyclerMenbros);
        recyclerMenbrosSelecionados = findViewById(R.id.recyclerMenbrosSelecionados);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        fabAvancarCadastro = findViewById(R.id.fabAvançarCadastro);
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("Membros",(Serializable) listaMenbrosSelecionados);
                startActivity(i);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurando adapter
        contatosAdapter = new ContatosAdapter(listaMenbros,getApplicationContext());

        //Configura recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMenbros.setLayoutManager(layoutManager);
        recyclerMenbros.setHasFixedSize(true);
        recyclerMenbros.setAdapter(contatosAdapter);

        recyclerMenbros.addOnItemTouchListener( new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMenbros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaMenbros.get(position);

                        //Remorevendo usuário selecionado
                        listaMenbros.remove(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        //Adicionado a lista de usuários selecionados
                        listaMenbrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        atualizarMenbrosToolbar();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configura recycler view para menbros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMenbrosSelecionados,getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);

        recyclerMenbrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMenbrosSelecionados.setHasFixedSize(true);
        recyclerMenbrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMenbrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMenbrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMenbrosSelecionados.get(position);

                        listaMenbrosSelecionados.remove(usuarioSelecionado);
                        listaMenbros.add(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        atualizarMenbrosToolbar();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));


    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerMenbros);
    }

    public void recuperarContatos (){

        valueEventListenerMenbros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for( DataSnapshot dados : dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail())) {


                        listaMenbros.add(usuario);

                    }

                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMenbrosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
