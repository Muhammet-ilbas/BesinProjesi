package com.batuhan.besinprojesi.viewModel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.batuhan.besinprojesi.model.Besin
import com.batuhan.besinprojesi.roomdb.BesinDatabase
import com.batuhan.besinprojesi.service.BesinAPIServis
import com.batuhan.besinprojesi.util.OzelSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BesinListesiViewModel(application : Application): AndroidViewModel(application) {

    val besinler = MutableLiveData<List<Besin>>()
    val besinHataMesaji = MutableLiveData<Boolean>()
    val besinYukleniyor = MutableLiveData<Boolean>()

    private val  besinApiServis = BesinAPIServis()
    private val  ozelSharedPreferences = OzelSharedPreferences(getApplication())

    private val guncellemeZamani = 10*60*1000*1000*1000L

     fun refreshData(){
        val kaydedilmeZamani = ozelSharedPreferences.zamaniAl()
        if (kaydedilmeZamani != null && kaydedilmeZamani !=0L && System.nanoTime()-kaydedilmeZamani<guncellemeZamani){
            verileriRoomdanAl()
        }else{
            verileriInternettenAl()
        }
    }
    fun refreshDataFromInternet(){
        verileriInternettenAl()
    }
    private fun verileriRoomdanAl(){
        besinYukleniyor.value = true
        viewModelScope.launch(Dispatchers.IO){
            val besinListesi = BesinDatabase(getApplication()).besinDao().getAllBesin()
            withContext(Dispatchers.Main){
                besinleriGoster(besinListesi)
                Toast.makeText(getApplication(),"Bu verileri roomdan aldık",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verileriInternettenAl(){
        besinYukleniyor.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val besinListesi = besinApiServis.getData()
            withContext(Dispatchers.Main){
                besinYukleniyor.value = false
                //rooma kaydedeceğiz
                roomaKaydet(besinListesi)
                Toast.makeText(getApplication(),"Besinleri internetten aldık,",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun besinleriGoster(besinListesi: List<Besin>){
        besinler.value = besinListesi
        besinHataMesaji.value = false
        besinYukleniyor.value = false
    }
    private fun roomaKaydet(besinListesi : List<Besin>){
        viewModelScope.launch {
            val dao = BesinDatabase(getApplication()).besinDao()
            dao.deleteAllBesin()

            val uuidListesi = dao.insertAll(*besinListesi.toTypedArray())

            var i = 0
            while (i<besinListesi.size){
                besinListesi[i].uuid = uuidListesi[i].toInt()
                i = i +1
            }
            besinleriGoster(besinListesi)
        }
        ozelSharedPreferences.zamaniKaydet(System.nanoTime())

    }

}