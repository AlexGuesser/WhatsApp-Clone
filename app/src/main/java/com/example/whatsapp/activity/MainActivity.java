package com.example.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.CadastroActivity;
import com.example.whatsapp.activity.LoginActivity;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        verificaUsuarioLogado();


    }


    public void verificaUsuarioLogado(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        if(autenticacao.getCurrentUser() != null){

            abrirTelaPrincipal();

        }else{

            abrirLoginCadastro();

        }

    }

    public void abrirTelaPrincipal(){

        startActivity(new Intent(this, PrincipalActivity.class));

    }

    public void abrirLoginCadastro(){

        startActivity(new Intent(this, LoginActivity.class));

    }

    @Override
    protected void onStart() {
        super.onStart();
        //autenticacao.signOut();
        verificaUsuarioLogado();
    }
}
