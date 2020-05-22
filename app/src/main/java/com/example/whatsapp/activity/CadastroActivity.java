package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome,campoEmail,campoSenha;
    private Button botaoCadastrar;
    private String idUsuario;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome   = findViewById(R.id.editNome);
        campoEmail  = findViewById(R.id.editEmail);
        campoSenha  = findViewById(R.id.editSenha);

        botaoCadastrar  = findViewById(R.id.buttonCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               String textoNome     = campoNome.getText().toString();
               String textoEmail    = campoEmail.getText().toString();
               String textoSenha    = campoSenha.getText().toString();

               if( !textoNome.isEmpty()){
                   if( !textoEmail.isEmpty()){
                       if( !textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);
                            cadastrarUsuario();

                       }else{
                           Toast.makeText(CadastroActivity.this, "Prencha o campo Senha", Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       Toast.makeText(CadastroActivity.this, "Prencha o campo E-mail", Toast.LENGTH_SHORT).show();
                   }
               }else{
                   Toast.makeText(CadastroActivity.this, "Prencha o campo Nome", Toast.LENGTH_SHORT).show();
               }

            }
        });

    }

    public void cadastrarUsuario(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(),usuario.getSenha())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setIdUsuario(idUsuario);
                    usuario.salvar();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();
                    startActivity(new Intent(CadastroActivity.this,PrincipalActivity.class));
                    Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar usuário",Toast.LENGTH_LONG).show();
                }else{

                    String excecao = "";

                    try {

                        throw task.getException();

                    } catch (FirebaseAuthWeakPasswordException e) {
                        excecao = "Digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Por favor, digite um e-mail válido";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excecao = "Esta conta já foi cadastrada!";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_LONG).show();

                }
            }
        });


    }

}
