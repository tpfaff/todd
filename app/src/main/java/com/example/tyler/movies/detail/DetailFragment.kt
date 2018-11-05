package com.example.tyler.movies.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.tyler.movies.Constants
import com.example.tyler.movies.R
import com.example.tyler.movies.detail.viewmodel.DetailFragmentViewModel
import com.example.tyler.movies.overview.model.UiState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_detail.*

/**
 * Copyright (c) 2018 Pandora Media, Inc.
 */
class DetailFragment : Fragment() {

    private lateinit var viewModel: DetailFragmentViewModel
    private val allSubscriptions = CompositeDisposable()

    companion object {
        val TAG = DetailFragment::class.java.simpleName
        val EXTRA_ID = "extra_id"
        fun newInstance(args: Bundle): Fragment {
            val fragment = DetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DetailFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.details_fragment_title)
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onStart() {
        super.onStart()
        allSubscriptions.add(
            viewModel.uiStateChanged
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ uiState ->
                    when (uiState) {
                        is UiState.Loading -> showLoadingView()
                        is UiState.DetailsReady -> showDetails(uiState)
                        is UiState.Error -> showErrorView()
                    }
                }, { error -> Log.e(TAG, "Couldn't display movie details", error) })
        )

        viewModel.getDetails(arguments!!.getString(EXTRA_ID)!!)
    }

    override fun onStop() {
        super.onStop()
        allSubscriptions.clear()
    }

    private fun showLoadingView() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun showErrorView() {
        progress_bar.visibility = View.GONE
        Toast.makeText(requireContext(), "Couldn't load details", Toast.LENGTH_LONG).show()
    }

    private fun showDetails(uiState: UiState.DetailsReady) {
        progress_bar.visibility = View.GONE
        Glide.with(this)
            .load("${Constants.poster_url}${uiState.details.backdrop_path}")
            .into(header_imageview)

        title_textview.text = uiState.details.title
        description_textview.text = uiState.details.overview
    }
}