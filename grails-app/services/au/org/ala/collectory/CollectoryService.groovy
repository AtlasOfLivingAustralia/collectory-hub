package au.org.ala.collectory

class CollectoryService {
    def isAddressEmpty(address) {
        if(address){
            return [address.street, address.postBox, address.city, address.state, address.postcode, address.country].every {!it}
        } else {
            true
        }
    }

    /**
     * Return all contacts for this group with the primary contact listed first filtered
     * to only include those with the 'public' attribute.
     *
     * @return list of ContactFor (contains the contact and the role for this collection)
     */
    List getPublicContactsPrimaryFirst(List contacts) {
        List list = contacts.findAll {it.contact.publish}
        if (list.size() > 1) {
            for (cf in list) {
                if (cf.primaryContact) {
                    // move it to the top
                    Collections.swap(list, 0, list.indexOf(cf))
                    break
                }
            }
        }
        return list
    }

    String buildName(Map contact) {
        if (contact.lastName)
            return [(contact.title ?: ''), (contact.firstName ?: ''), contact.lastName].join(" ").trim()
        else if (contact.email)
            return contact.email
        else if (contact.phone)
            return contact.phone
        else if (contact.mobile)
            return contact.mobile
        else if (contact.fax)
            return contact.fax
        else
            return ''
    }

    /**
     * List the uids that identify this institution and all its descendant institutions.
     *
     * @return list of UID
     */
    List<String> descendantUids(Map institution) {
        def uids = [institution.uid]
        if (institution.childInstitutions) {
            institution.childInstitutions.each {
                def child = _get(it as String)
                if (child) {
                    uids += child.descendantUids()
                }
            }
        }
        return uids
    }

    /**
     * List the data resource uids linked to this institution
     *
     * @return list of UID
     */
    List<String> descendantDataResources(Map institution) {
        def uids = []
        if (institution.linkedRecordProviders) {
            institution.linkedRecordProviders.each {
                uids.push(it.uid)
            }
        }
        return uids
    }
}
