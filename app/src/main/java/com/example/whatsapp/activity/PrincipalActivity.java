package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.fragment.ContatosFragment;
import com.example.whatsapp.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class PrincipalActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );

        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        //Configuração do search view
        searchView = findViewById(R.id.materialSearchPrincipal);

        //Listener para o search view
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();

            }
        });

        //Listener para a caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                switch (viewPager.getCurrentItem()){

                    case 0:

                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        if(newText != null && !newText.isEmpty()){
                           conversasFragment.pesquisarConversas(newText);
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                        break;
                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        if(newText != null && !newText.isEmpty()){
                            contatosFragment.pesquisarContatos(newText);
                        }else{
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal,menu);

        //Configurando botão de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.menuSair:
                deslogarUsuario();
                finish();
                break;

            case R.id.menuConfigurações:
                abrirConfiguracoes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){

        try{
            autenticacao.signOut();
        }catch (Exception e ){
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(PrincipalActivity.this,ConfiguracoesActivity.class);
        startActivity(intent);
    }

}
