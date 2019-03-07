package org.rfcx.ranger

import androidx.test.runner.AndroidJUnit4
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.guardian.Site
import org.rfcx.ranger.localdb.SiteGuardianDb

@RunWith(AndroidJUnit4::class)
class SiteGuardianDbTest {

    @Test
    fun canSaveOneSite() {
        // Arrange
        val realm = realm()
        val db = SiteGuardianDb(realm)
        val site = randomSite()

        // Act
        db.saveSites(listOf(site))

        // Assert
        Assert.assertEquals(1, realm.where(Site::class.java).count())
    }

    @Test
    fun canSaveMultipleSites() {
        // Arrange
        val realm = realm()
        val db = SiteGuardianDb(realm)
        val sites = listOf(randomSite(), randomSite(), randomSite())

        // Act
        db.saveSites(sites)

        // Assert
        Assert.assertEquals(sites.size, realm.where(Site::class.java).count().toInt())
    }

    @Test
    fun canGetSite() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val site = randomSite()
        db.saveSites(listOf(randomSite(), site, randomSite(), randomSite()))

        // Act
        val result = db.site(site.id)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(site.id, result?.id)
        Assert.assertEquals(site.name, result?.name)
        Assert.assertEquals(site.description, result?.description)
    }

    @Test
    fun canUpdateSite() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val site1 = randomSite()
        val site2 = randomSite()
        db.saveSites(listOf(site1, site2))
        val updatedSite1 = randomSite()
        updatedSite1.id = site1.id

        // Act
        db.saveSites(listOf(updatedSite1, site2))

        // Assert
        val result = db.site(site1.id)
        Assert.assertNotNull(result)
        Assert.assertEquals(updatedSite1.name, result?.name)
        Assert.assertEquals(updatedSite1.description, result?.description)
    }

    @Test
    fun canReplaceSites() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val site1 = randomSite()
        val site2 = randomSite()
        db.saveSites(listOf(site1, site2))
        val site3 = randomSite()
        val site4 = randomSite()
        val site5 = randomSite()

        // Act
        db.saveSites(listOf(site3, site4, site5))

        // Assert
        Assert.assertNull(db.site(site1.id))
        Assert.assertNull(db.site(site2.id))
        Assert.assertEquals(3, db.sites().size)
    }

    @Test
    fun canDeleteSite() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val site = randomSite()
        db.saveSites(listOf(randomSite(), site, randomSite(), randomSite()))

        // Act
        db.saveSites(listOf(randomSite(), randomSite(), randomSite()))

        // Assert
        val result = db.site(site.id)
        Assert.assertNull(result)
    }

    @Test
    fun canKeepOtherSitesWhenDeleteOneSite() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val site1 = randomSite()
        val site2 = randomSite()
        val toBeDeletedSite = randomSite()
        db.saveSites(listOf(site1, site2, toBeDeletedSite))

        // Act
        db.saveSites(listOf(site2, site1))

        // Assert
        Assert.assertNull(db.site(toBeDeletedSite.id))
        Assert.assertNotNull(db.site(site1.id))
        Assert.assertNotNull(db.site(site2.id))
        Assert.assertEquals(2, db.sites().size)
    }


    @Test
    fun canSaveOneGuardianGroup() {
        // Arrange
        val realm = realm()
        val db = SiteGuardianDb(realm)
        val group = randomGuardianGroup()

        // Act
        db.saveGuardianGroups(listOf(group))

        // Assert
        Assert.assertEquals(1, realm.where(GuardianGroup::class.java).count())
    }

    @Test
    fun canSaveMultipleGuardianGroups() {
        // Arrange
        val realm = realm()
        val db = SiteGuardianDb(realm)
        val groups = listOf(randomGuardianGroup(), randomGuardianGroup(), randomGuardianGroup())

        // Act
        db.saveGuardianGroups(groups)

        // Assert
        Assert.assertEquals(groups.size, realm.where(GuardianGroup::class.java).count().toInt())
    }

    @Test
    fun canGetGuardianGroup() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val group = randomGuardianGroup()
        db.saveGuardianGroups(listOf(randomGuardianGroup(), randomGuardianGroup(), group, randomGuardianGroup()))

        // Act
        val result = db.guardianGroup(group.shortname)

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals(group.shortname, result?.shortname)
        Assert.assertEquals(group.name, result?.name)
        Assert.assertEquals(group.description, result?.description)
    }

    @Test
    fun canUpdateGuardianGroup() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val group1 = randomGuardianGroup()
        val group2 = randomGuardianGroup()
        db.saveGuardianGroups(listOf(group1, group2))
        val updatedGroup1 = randomGuardianGroup()
        updatedGroup1.shortname = group1.shortname

        // Act
        db.saveGuardianGroups(listOf(updatedGroup1, group2))

        // Assert
        val result = db.guardianGroup(group1.shortname)
        Assert.assertNotNull(result)
        Assert.assertEquals(updatedGroup1.name, result?.name)
        Assert.assertEquals(updatedGroup1.description, result?.description)
    }

    @Test
    fun canReplaceGuardianGroups() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val group1 = randomGuardianGroup()
        db.saveGuardianGroups(listOf(group1))
        val group2 = randomGuardianGroup()
        val group3 = randomGuardianGroup()
        val group4 = randomGuardianGroup()

        // Act
        db.saveGuardianGroups(listOf(group2, group3, group4))

        // Assert
        Assert.assertNull(db.guardianGroup(group1.shortname))
        Assert.assertEquals(3, db.guardianGroups().size)
    }

    @Test
    fun canDeleteGuardianGroup() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val group = randomGuardianGroup()
        db.saveGuardianGroups(listOf(group, randomGuardianGroup(), randomGuardianGroup(), randomGuardianGroup()))

        // Act
        db.saveGuardianGroups(listOf(randomGuardianGroup(), randomGuardianGroup(), randomGuardianGroup()))

        // Assert
        Assert.assertNull(db.guardianGroup(group.shortname))
    }

    @Test
    fun canKeepOtherGuardianGroupsWhenDeleteOneGuardianGroup() {
        // Arrange
        val db = SiteGuardianDb(realm())
        val group1 = randomGuardianGroup()
        val group2 = randomGuardianGroup()
        val groupToBeDeleted = randomGuardianGroup()
        db.saveGuardianGroups(listOf(group1, group2, groupToBeDeleted))

        // Act
        db.saveGuardianGroups(listOf(group1, group2))

        // Assert
        Assert.assertNull(db.guardianGroup(groupToBeDeleted.shortname))
        Assert.assertNotNull(db.guardianGroup(group1.shortname))
        Assert.assertNotNull(db.guardianGroup(group2.shortname))
        Assert.assertEquals(2, db.guardianGroups().size)
    }

    private fun realm(): Realm {
        val config = RealmConfiguration.Builder()
                .name("myrealm.realm")
                .inMemory()
                .build()
        return Realm.getInstance(config)
    }

    private fun randomSite(): Site {
        val site = Site()
        site.id = randomAlphanumeric(8)
        site.name = randomAlphanumeric(20)
        site.description = randomAlphanumeric(50)
        return site
    }

    private fun randomGuardianGroup(): GuardianGroup {
        val group = GuardianGroup()
        group.shortname = randomAlphanumeric(8)
        group.name = randomAlphanumeric(20)
        group.description = randomAlphanumeric(50)
        return group
    }

}