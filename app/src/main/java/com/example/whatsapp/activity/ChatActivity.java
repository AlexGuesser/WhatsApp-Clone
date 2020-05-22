package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Mensagem;
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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private EditText editMensagem;
    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens =  new ArrayList<>();
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListener;
    private ImageView imageCamera,imageGaleria;
    private Grupo grupo;
    private Usuario usuarioRemetente;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    //Configurando usuarios remetente e destinatáario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configuração da ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações Iniciais
        textViewNome        = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem        = findViewById(R.id.editMensagem);
        recyclerMensagens   = findViewById(R.id.recyclerMensagens);
        imageCamera         = findViewById(R.id.imageCamera);
        imageGaleria        = findViewById(R.id.imageGaleria);

        //Recuperando dados dos usuários
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();


        //Recuperando os dados do usuário Selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            if(bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();

                textViewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if(foto!=null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }


            }else{
                //***********
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if(foto!=null){
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
                //**********
            }
        }

        //Configurando o adapter
        adapter = new MensagensAdapter(mensagens,getApplicationContext());


        //Configurando o recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize( true);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child(idUsuarioDestinatario);

        //Evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_CAMERA);
                }

            }
        });

        imageGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Bitmap imagem = null;

            try {

                switch (requestCode) {

                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //Arrumando os dados da imagem para enviar para o Firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagem.compress(Bitmap.CompressFormat.JPEG,50,baos);
                byte[] dadosImagem = baos.toByteArray();

                //Criando nome das imagens
                String nomeImagem = UUID.randomUUID().toString();

                //Configurando referencia no storage
                StorageReference imagemRef = storage.child("imagens")
                                                    .child("fotos")
                                                    .child(idUsuarioRemetente)
                                                    .child(nomeImagem);

                UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Erro","Erro ao fazer upload");
                        Toast.makeText(ChatActivity.this, "Erro ao enviar a imagem", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());
                        String url = uri.getResult().toString();

                        if(usuarioDestinatario!=null){//Chat entre duas pessoas

                            Mensagem mensagem = new Mensagem();
                            mensagem.setIdUsuario(idUsuarioRemetente);
                            mensagem.setImagem(url);
                            mensagem.setMensagem("imagem.jpeg");

                            salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                            salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                            salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,usuarioDestinatario,mensagem,false);
                            salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioRemetente,mensagem,false);

                        }else{//Chat em grupo

                            for( Usuario membro: grupo.getMenbros()){

                                String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                Mensagem mensagem =  new Mensagem();
                                mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setNome(usuarioRemetente.getNome());
                                mensagem.setImagem(url);

                                salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);

                                salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario, mensagem,true);

                            }


                        }
                        Toast.makeText(ChatActivity.this, "Sucesso ao enviar a imagem", Toast.LENGTH_SHORT).show();

                    }
                });


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if(  !textoMensagem.isEmpty()){

            if(usuarioDestinatario!=null){
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                //Limpando o texto
                editMensagem.setText("");

                //Salvar conversa
                salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,usuarioDestinatario,mensagem,false);
                salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioRemetente,mensagem,false);
            }else{

                for( Usuario membro: grupo.getMenbros()){

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem =  new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);

                    salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario, mensagem,true);

                    //Limpando o texto
                    editMensagem.setText("");


                }

            }
        }else{

            Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar", Toast.LENGTH_SHORT).show();

        }

    }

    private void salvarMensagem(String idRemetente,String idDestinatario,Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListener);
    }

    private void recuperarMensagens(){

        childEventListener = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void salvarConversa(String idRemetente,String idDestinatario,Usuario usuarioExibicao,Mensagem msg,boolean isGroup){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if(isGroup){
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);

        }else{
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");
        }

        conversaRemetente.salvar();
    }

    public void onResume() {
        super.onResume();
        mensagens.clear();
    }

}
