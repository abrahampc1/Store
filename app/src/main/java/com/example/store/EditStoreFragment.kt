package com.example.store

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.window.OnBackAnimationCallback
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.store.databinding.FragmentEditStoreBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
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
            mIsEditMode = false
            mStoreEntity = StoreEntity(Name = "", Phone = "", photoUrl = "")
        }

        setupActionBar()

        setupTextFields()
    }

    private fun setupActionBar() {
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = if (mIsEditMode) getString(R.string.edit_store_title_edit)
                                            else getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)
    }

    private fun setupTextFields() {
        with(mBinding){
            etName.addTextChangedListener { validateFields(tilName) }
            etPhone.addTextChangedListener { validateFields(tilPhone) }
            imgPhotoURL.addTextChangedListener {
                validateFields(tilPhotoUrl)
                loadImage(it.toString().trim())
            }
        }
    }

    private fun loadImage(url : String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imgPhoto)
    }

    private fun getStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity?>()
        Thread{
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()
        queue.take()?.let {setUiStore(it) }
    }

    private fun setUiStore(storeEntity: StoreEntity) {
        with(mBinding){
            etName.text = storeEntity.Name.editable()
            etPhone.text = storeEntity.Phone.editable()
            etWebSite.text = storeEntity.WebSite.editable()
            imgPhotoURL.text = storeEntity.photoUrl.editable()
        }
    }

   private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.dialog_exist_title)
                    .setMessage(R.string.dialog_exist_message)
                    .setPositiveButton(R.string.dialog_exit_ok){_, _->
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                    .setNegativeButton(R.string.dialog_delete_cancel, null)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_save -> {
                if (mStoreEntity != null && validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone, mBinding.tilName)){
                    /*
               val store = StoreEntity(Name = mBinding.etName.text.toString().trim(),
                   Phone = mBinding.etPhone.text.toString().trim(),
                   WebSite = mBinding.etWebSite.text.toString().trim(),
                   photoUrl = mBinding.imgPhotoURL.text.toString().trim())

                */

                    with(mStoreEntity!!){
                        Name = mBinding.etName.text.toString().trim()
                        Phone = mBinding.etPhone.text.toString().trim()
                        WebSite = mBinding.etWebSite.text.toString().trim()
                        photoUrl = mBinding.imgPhotoURL.text.toString().trim()
                    }

                    val queue = LinkedBlockingQueue<StoreEntity>()

                    thread {
                        if (mIsEditMode) StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        queue.add(mStoreEntity)
                        start()
                    }

                    with(queue.take()){
                        hideKeyboard()
                        if (mIsEditMode){
                            mActivity?.updateStore(mStoreEntity!!)

                            Snackbar.make(mBinding.root,
                                R.string.edit_store_message_update_success,
                            Snackbar.LENGTH_SHORT).show()
                        } else {
                            mActivity?.addStore(this)
                            Snackbar.make(mBinding.root, R.string.edit_store_save_message_success,
                                Snackbar.LENGTH_SHORT).show()

                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        //return super.onOptionsItemSelected(item)
    }

    private fun validateFields(vararg textFields : TextInputLayout) : Boolean {
        var isValid = true

        for (textField in textFields){
            if (textField.editText?.text.toString().trim().isEmpty()){
                textField.error = getString(R.string.helper_required)
                isValid = false
            } else textField.error = null
        }

        if (!isValid) Snackbar.make(mBinding.root,
            R.string.edit_store_save_message_valid,
            Snackbar.LENGTH_SHORT).show()

        return isValid
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (mBinding.imgPhotoURL.text.toString().trim().isEmpty()){
            mBinding.tilPhotoUrl.error = getString(R.string.helper_required)
            mBinding.imgPhotoURL.requestFocus()
            isValid = false
        }

        if (mBinding.etPhone.text.toString().trim().isEmpty()){
            mBinding.tilPhone.error = getString(R.string.helper_required)
            mBinding.etPhone.requestFocus()
            isValid = false
        }

        if (mBinding.etName.text.toString().trim().isEmpty()){
            mBinding.tilName.error = getString(R.string.helper_required)
            mBinding.etName.requestFocus()
            isValid = false
        }

        return isValid
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
