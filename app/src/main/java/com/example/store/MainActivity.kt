package com.example.store

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.store.R.*
import com.example.store.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity(), OnClickListener, MainAux {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.fab.setOnClickListener{ launchEditFragment() }

        setupRecyclerView()
    }

    private fun launchEditFragment(args : Bundle? = null) {
        val fragment = EditStoreFragment()
        if (args != null) fragment.arguments = args

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.add(id.containerMain, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        //mBinding.fab.hide()

        hideFab()
    }
    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this,resources.getInteger(R.integer.main_columns))
        getStores()

        mBinding.RecyvlerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    fun getStores(){

        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()

        Thread{
            val stores = StoreApplication.database.storeDao().getAllStores()
            queue.add(stores)
        }.start()

        mAdapter.setStores(queue.take())
    }
    /**
     * OnClickListener
     * **/
    override fun OnClick(storeId: Long) {
        val args = Bundle()
        args.putLong(getString(R.string.id_args), storeId)

        launchEditFragment(args)
    }

    override fun onFavoriteStore(storeEntity: StoreEntity) {
       storeEntity.isFavorite = !storeEntity.isFavorite
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread{
            StoreApplication.database.storeDao().updateStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        updateStore(queue.take())
    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        val items = resources.getStringArray(R.array.array_option_item)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_option_title)
            .setItems(items) { _, i ->
                when(i){
                    0 -> confirmDelete(storeEntity)
                    1 -> dial(storeEntity.Phone)
                    2 -> goToWebsite(storeEntity.WebSite)
                }
            }
            .show()
    }

    private fun confirmDelete(storeEntity: StoreEntity){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.delete_title_confirm) { _, _ ->

            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread{
            StoreApplication.database.storeDao().deleteStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        mAdapter.delete(queue.take())
    }

    private fun dial(phone : String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel:$phone")
        }

        startIntent(callIntent)
    }

    private fun goToWebsite(website : String){
        if (website.isEmpty()){
            Toast.makeText(this, R.string.main_error_no_website, Toast.LENGTH_LONG).show()
        }else {
            val websiteIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(website)
            }

            startIntent(websiteIntent)
        }
    }

    private fun startIntent(intent: Intent){
        if (intent.resolveActivity(packageManager) != null)
            startActivity(intent)
        else
            Toast.makeText(this, R.string.main_error_no_resolve, Toast.LENGTH_LONG).show()
    }

    /***
     * Main Aux
     * ***/

    override fun hideFab(isVisible: Boolean) {
        if (isVisible) mBinding.fab.show() else mBinding.fab.hide()
    }

    override fun addStore(storeEntity: StoreEntity) {
        mAdapter.add(storeEntity)
    }

    override fun updateStore(storeEntity: StoreEntity) {
        mAdapter.update(storeEntity)
    }
}
