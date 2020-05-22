package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMenbrosSelecionados = new ArrayList<>();
    private Toolbar toolbar;
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;

    private static final int SELECAO_GALERIA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);

        //Configurações iniciais
        textTotalParticipantes = findViewById(R.id.textTotalParticipantes2);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imageViewFotoGrupo);
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();
        fabSalvarGrupo = findViewById(R.id.fabSalvarGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);

        //Evento de clique em cima da foto do Grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");


        //Recuperando lista de menbros passada
        if(getIntent().getExtras() != null){

            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("Membros");
            listaMenbrosSelecionados.addAll(membros);

            textTotalParticipantes.setText("Participantes: " + listaMenbrosSelecionados.size());


        }

        //Configura recycler view para membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMenbrosSelecionados,getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);

        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String nomeGrupo = editNomeGrupo.getText().toString();

                listaMenbrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());

                grupo.setMenbros(listaMenbrosSelecionados);
                grupo.setNome(nomeGrupo);

                grupo.salvar();

                finish();
                //CadastroGrupoActivity.this.getParent().finish();


                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo",grupo);
                startActivity(i);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            Bitmap imagem = null;

            try{

                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);

                if(imagem!=null){

                    imageGrupo.setImageBitmap(imagem);

                    //Recuperando dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvando imagem no Firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            String url = uri.getResult().toString();
                            grupo.setFoto(url);

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }

    }
    }
}
