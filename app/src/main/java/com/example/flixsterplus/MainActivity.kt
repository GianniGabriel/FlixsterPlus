package com.example.flixsterplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.bumptech.glide.Glide
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Movie(
    val title: String,
    val poster_path: String,
    val overview: String
)

class MovieAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    interface MovieApiService {
        @GET("movie/now_playing")
        suspend fun getNowPlayingMovies(
            @Query("api_key") apiKey: String,
            @Query("language") language: String
        ): Response<NowPlayingResponse>
    }

    data class NowPlayingResponse(
        val results: List<Movie>
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleTextView.text = movie.title
        holder.descriptionTextView.text = movie.overview
        // Load the poster image using Glide or any other image loading library
        Glide.with(holder.itemView)
            .load("https://image.tmdb.org/t/p/w500/${movie.poster_path}")
            .into(holder.posterImageView)
    }

    override fun getItemCount() = movies.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.poster_image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        val descriptionTextView = itemView.findViewById<TextView>(R.id.description_text_view)
    }
}


class MainActivity : AppCompatActivity() {

    private lateinit var movieApiService: MovieAdapter.MovieApiService
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var movieRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        movieApiService = retrofit.create(MovieAdapter.MovieApiService::class.java)
        movieAdapter = MovieAdapter(emptyList())
        movieRecyclerView = findViewById(R.id.movie_recycler_view)
        movieRecyclerView.adapter = movieAdapter
        movieRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        GlobalScope.launch(Dispatchers.Main) {
            val response = movieApiService.getNowPlayingMovies(
                apiKey = "a07e22bc18f5cb106bfe4cc1f83ad8ed",
                language = "en-US"
            )
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                movieAdapter = MovieAdapter(movies)
                movieRecyclerView.adapter = movieAdapter
            } else {
                print("problem fetching API")
            }
        }
    }
}
