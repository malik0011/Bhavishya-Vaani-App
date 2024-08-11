package com.example.astrochart.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.astrochart.data.Prediction
import com.example.astrochart.databinding.ItemPredictionBinding

class HorizontalPredictionListAdapter(private val onClick: (Prediction) -> Unit):
    ListAdapter<Prediction, HorizontalPredictionListAdapter.PredictionsViewHolder>(PredictionDiffCallback) {
    class PredictionsViewHolder(private val binding: ItemPredictionBinding, val onClick: (Prediction) -> Unit) : RecyclerView.ViewHolder(binding.root) {
            fun bind(prediction: Prediction) {
                binding.root.setOnClickListener {
                    onClick(prediction)
                }

                binding.apply {
                    predictionText.text = prediction.name
                }
            }
    }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionsViewHolder {
            val view = ItemPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PredictionsViewHolder(view, onClick)
        }

        /* Gets current flower and uses it to bind view. */
        override fun onBindViewHolder(holder: PredictionsViewHolder, position: Int) {
            val flower = getItem(position)
            holder.bind(flower)

        }
    }

    object PredictionDiffCallback : DiffUtil.ItemCallback<Prediction>() {
        override fun areItemsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
            return oldItem.name == newItem.name
        }
    }