package com.example.seamstress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seamstress.R
import com.example.seamstress.data.CustomersRepository
import com.example.seamstress.domain.SeamstressDataBase
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.customers.CustomersDao
import com.example.seamstress.domain.dresses.DressDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

class SeamstressViewModel(application: Application) : AndroidViewModel(application) {

    val getAllCustomers: LiveData<List<Customers>>
    private val customerRepo: CustomersRepository
    private val dressDao: DressDao

    init {
        val db = SeamstressDataBase.getDatabase(application)

        val customersDao: CustomersDao = db.customersDao()
        customerRepo = CustomersRepository(customersDao)
        dressDao = db.dressDao()
        getAllCustomers = customerRepo.getAllCustomers
    }

    private val _formState = MutableLiveData(CustomerFormState())
    val formState: LiveData<CustomerFormState> = _formState

    /** Заполняет форму при редактировании. */
    fun fillForm(customer: Customers) {
        _formState.value = CustomerFormState(
            name = customer.name,
            phone = customer.phone.orEmpty(),
            balance = customer.balance.toString()
        )
    }

    fun onNameChange(text: String) {
        _formState.value = state().copy(name = text, nameError = false)
    }

    fun onPhoneChange(text: String) {
        _formState.value = state().copy(phone = filterPhone(text), phoneError = false)
    }

    fun onBalanceChange(text: String) {
        _formState.value = state().copy(balance = filterBalance(text), balanceError = false)
    }

    private fun state(): CustomerFormState = _formState.value ?: CustomerFormState()

    /** Клиент по id из Room: LiveData обновляется сам после правки и удаления. */
    fun customerById(id: Long): LiveData<Customers?> = customerRepo.getCustomerByIdLiveData(id)

    private val _newCustomerId = Channel<Long>(Channel.BUFFERED)
    val newCustomerId: Flow<Long> = _newCustomerId.receiveAsFlow()

    private val _customerSaved = Channel<Long>(Channel.BUFFERED)
    val customerSaved: Flow<Long> = _customerSaved.receiveAsFlow()

    private val _saveFailed = Channel<Unit>(Channel.BUFFERED)
    val saveFailed: Flow<Unit> = _saveFailed.receiveAsFlow()

    /**
     * Проверяет форму и пишет клиента одной операцией: [id] = 0 — новый клиент,
     * иначе обновление. Пока идёт запись, форма заблокирована ([CustomerFormState.saving]).
     */
    fun saveCustomer(id: Long = 0L) {
        val current = state()
        if (current.saving) return

        val name = current.name.trim()
        val phone = parsePhone(current.phone)
        val balance = parseBalance(current.balance)
        val nameError = name.isEmpty()
        val phoneError = phone == null
        val balanceError = balance == null
        if (nameError || phoneError || balanceError) {
            _formState.value = current.copy(
                nameError = nameError,
                phoneError = phoneError,
                balanceError = balanceError
            )
            return
        }

        _formState.value = current.copy(saving = true)
        viewModelScope.launch {
            try {
                val customer = Customers(
                    id = id,
                    name = name,
                    phone = phone!!.ifEmpty { null },
                    img = R.drawable.ic_baseline_add_a_photo_24,
                    balance = balance!!
                )
                if (id == 0L) {
                    _newCustomerId.send(customerRepo.insertCustomer(customer))
                } else {
                    customerRepo.updateCustomer(customer)
                    _customerSaved.send(id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveFailed.send(Unit)
            } finally {
                _formState.value = state().copy(saving = false)
            }
        }
    }

    private val _customerDeleted = Channel<Unit>(Channel.BUFFERED)
    val customerDeleted: Flow<Unit> = _customerDeleted.receiveAsFlow()

    /** Удаление клиента: замеры и изделия уходят каскадом, файлы фото изделий убираются. */
    fun deleteCustomer(client: Customers) {
        viewModelScope.launch(Dispatchers.IO) {
            val photos = dressDao.getPhotosByCustomerId(client.id)
            customerRepo.deleteCustomer(client)
            photos.forEach { runCatching { File(it.path).delete() } }
            _customerDeleted.send(Unit)
        }
    }
}
