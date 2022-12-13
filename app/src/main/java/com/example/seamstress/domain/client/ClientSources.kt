package com.example.seamstress.domain.client

import com.example.seamstress.R

class ClientSources {
    private val clients: ArrayList<Clients> = arrayListOf()

    fun bindClientData():ArrayList<Clients> {
        clients.add(Clients(0, "Zoi Gothi Hamam", "+7 987 654 32 10", R.drawable.shifon,0F))
        clients.add(Clients(0, "Hoi", "+7 987 654 32 11", R.drawable.jane,100F))
        clients.add(Clients(0, "Joi", "+7 987 654 32 12", R.drawable.ic_baseline_add_a_photo_24,0F))
        clients.add(Clients(0, "Koi", "+7 987 654 32 13", R.drawable.ic_baseline_add_a_photo_24,200F))
        clients.add(Clients(0, "Loi", "+7 987 654 32 14", R.drawable.ic_baseline_add_a_photo_24,0F))
        clients.add(Clients(0, "Xoi", "+7 987 654 32 15", R.drawable.ic_baseline_add_a_photo_24,313F))
        clients.add(Clients(0, "Coi", "+7 987 654 32 16", R.drawable.ic_baseline_add_a_photo_24,0F))

        return clients
    }
}