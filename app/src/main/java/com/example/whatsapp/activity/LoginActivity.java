package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private TextView textCadastrar;
    private EditText campoEmail,campoSenha;
    private Button botaoLogar;
    private Usuario usuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textCadastrar   = findViewById(R.id.textCadastrar);
        campoEmail      = findViewById(R.id.editEmail);
        campoSenha      = findViewById(R.id.editSenha);
        botaoLogar      = findViewById(R.id.buttonLogar);

        botaoLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoEmail   = campoEmail.getText().toString();
                String textoSenha   = campoSenha.getText().toString();

                if(!textoEmail.isEmpty()){
                    if(!textoSenha.isEmpty()){

                        usuario = new Usuario();
                        usuario.setEmail( textoEmail);
                        usuario.setSenha(textoSenha);
                        validarLogin();

                    }else{
                        Toast.makeText(LoginActivity.this,"Preencha a senha",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"Preencha o email",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void validarLogin(){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    abrirTelaPrincipal();

                }else{

                    String excecao = "";

                    try {

                        throw task.getException();

                    }
                    catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Senha incorreta";
                    }
                    catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não está cadastrado";
                    }
                    catch (Exception e) {
                        excecao = "Erro ao realizar login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,
                            excecao,
                            Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    public void cadastrar(View view){

        startActivity(new Intent(this, CadastroActivity.class));

    }

    public void abrirTelaPrincipal(){

        startActivity(new Intent(this, PrincipalActivity.class));

    }

}
