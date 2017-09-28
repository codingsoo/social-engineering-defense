import json
import os

# json file download directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

data = ["antifraudintl_adoption_scams.json", "antifraudintl_animal_scams.json", "antifraudintl_atm_card_scams.json", "antifraudintl_banking_scams.json", "antifraudintl_business_scams.json", "antifraudintl_central_bank_of_nigeria_scams.json",  "antifraudintl_church_and_charity_scams.json", "antifraudintl_commodity_scams.json", "antifraudintl_compensation_scams.json", "antifraudintl_conference_scams.json", "antifraudintl_debt_collection_scams.json", "antifraudintl_diplomat_scams.json", "antifraudintl_donation_appeal_scams.json", "antifraudintl_dying_persons.json", "antifraudintl_education_scams.json", "antifraudintl_employment_scams.json", "antifraudintl_extortion_scams.json", "antifraudintl_finance_and_trading_scams.json", "antifraudintl_funeral_notice_scams.json", "antifraudintl_government_scams.json", "antifraudintl_hotel_scams.json", "antifraudintl_internet_sales_scam.json", "antifraudintl_legal_counsel_(collection)_fake_check_scams.json","antifraudintl_legal_notice_scams.json", "antifraudintl_loan_scams.json", "antifraudintl_lottery_scams.json","antifraudintl_medical_scams.json", "antifraudintl_military_scams.json", "antifraudintl_miscellaneous.json", "antifraudintl_money_transfer_scams.json", "antifraudintl_mystery_shopper.json", "antifraudintl_next_of_kin.json", "antifraudintl_orphans.json", "antifraudintl_phishing.json", "antifraudintl_product_purchase_scams.json", "antifraudintl_real_estate_scams.json", "antifraudintl_recovery_scams.json", "antifraudintl_refugee_scams.json", "antifraudintl_rental_scams.json", "antifraudintl_representative_(fake_check)_scams.json", "antifraudintl_spellcaster_scams.json", "antifraudintl_stranded_traveler_scams.json", "antifraudintl_virtual_money_scams.json", "antifraudintl_visa_scams.json", "antifraudintl_widows.json", "scamalot_artists_and_artworks_scam.json", "scamalot_auction_ebay_scam.json", "scamalot_blackmail_extortion_threats.json", "scamalot_business_venture_scam.json", "scamalot_charity_scam.json", "scamalot_classified_ads_scam.json", "scamalot_conference_seminar_scam.json", "scamalot_dating_scam.json", "scamalot_fake_counterfeit_goods.json", "scamalot_identity_theft.json", "scamalot_job_scam.json", "scamalot_lottery_scam.json", "scamalot_malware_trojans_virus_payloads.json", "scamalot_nanny_au_pair_scam.json", "scamalot_nigerian_419_scam.json", "scamalot_generic_unspecified_scam.json", "scamalot_overpayment_scam.json", "scamalot_pet_adoption_scam.json", "scamalot_defective_products_services.json", "scamalot_rental_scam.json", "scamalot_scentsy_order_scam.json", "scamalot_secret_mystery_shopper_scam.json", "scamalot_social_network_scam.json", "scamalot_wills_probate_scam.json", "scamwarners_rental_scams.json", "scamwarners_lottery_scams.json", "scamwarners_romance_scams.json",
"scamwarners_loan_scams.json", "scamwarners_employment_scams.json", "scamwarners_au_pair_scams.json", "scamwarners_recovery_scams.json", "scamwarners_charity_scams.json", "scamwarners_conference_scams.json", "scamwarners_various_financial_scams.json", "scamwarners_fake_sites_used_for_fraud.json", "scamwarners_pet_scams.json", "scamwarners_email_scams_targeting_businesses.json"]

merged_data = {}
count = 0
for scam_data in data:
    print(scam_data)
    flag = 0
    with open(os.path.join(BASE_DIR, scam_data), 'r') as f:
        scam = json.load(f)

        for title, content in scam.items():
            if flag == 1:
                merged_data[title] = content
                count = count + 1
            flag = 1


with open(os.path.join(BASE_DIR, 'merged_scams.json'), 'w+') as json_file:
    json.dump(merged_data, json_file)

print(count,"file dumped in merged_scams")
