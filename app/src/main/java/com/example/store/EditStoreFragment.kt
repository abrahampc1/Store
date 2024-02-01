package com.example.store

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.store.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.NonCancellable.start
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity : MainActivity? = null
    private var mIsEditMode : Boolean = false
    private var mStoreEntity : StoreEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_store, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.id_args), 0)
        if (id != null && id != 0L){
            mIsEditMode = true
            getStore(id)
        } else {
            Toast.makeText(activity, id.toString(), Toast.LENGTH_SHORT).show()
        }

        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)

        mBinding.imgPhotoURL.addTextChangedListener{
            Glide.with(this)
                .load(mBinding.imgPhotoURL.text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)
        }
    }

    private fun getStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity?>()
        Thread{
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()
        queue.take()?.let {
            
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressedDispatcher?.onBackPressed()
                true
            }
            R.id.action_save -> {
                val store = StoreEntity(Name = mBinding.etName.text.toString().trim(),
                    Phone = mBinding.etPhone.text.toString().trim(),
                    WebSite = mBinding.etWebSite.text.toString().trim(),
                    photoUrl = mBinding.imgPhotoURL.text.toString().trim())

                val queue = LinkedBlockingQueue<Long?>()

                thread {
                    mActivity?.addStore(store)
                    hideKeyboard()
                    val id = StoreApplication.database.storeDao().addStore(store)
                    queue.add(id)
                    start()
                }

                queue.take()?.let{

                    Toast.makeText(mActivity, R.string.edit_store_save_message_success, Toast.LENGTH_SHORT).show()
                    mActivity?.onBackPressedDispatcher?.onBackPressed()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return super.onOptionsItemSelected(item)
    }

    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
  override fun onDestroyView(){
        hideKeyboard()
        super.onDestroyView()
    }
    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)
        setHasOptionsMenu(false)
        super.onDestroy()
    }
}
